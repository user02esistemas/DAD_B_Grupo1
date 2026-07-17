import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';

import '../../../core/config/app_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../../auth/service/auth_service.dart';
import '../../auth/view/login_page.dart';
import '../../caja/view/caja_page.dart';
import '../../compras/view/compras_page.dart';
import '../../productos/view/products_page.dart';
import '../../reportes/view/reportes_page.dart';
import '../../usuarios/view/usuarios_page.dart';
import '../../ventas/view/ventas_page.dart';
import '../model/dashboard_summary.dart';
import '../service/dashboard_service.dart';
import '../viewmodel/dashboard_viewmodel.dart';

class HomePage extends StatefulWidget {
  const HomePage({required this.user, super.key});

  final User user;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  late final DashboardViewModel _viewModel;
  WebSocket? _notificationSocket;
  Timer? _reconnectTimer;
  final List<_RealtimeNotification> _realtimeNotifications = [];
  int _unreadNotifications = 0;

  @override
  void initState() {
    super.initState();
    _viewModel = DashboardViewModel(DashboardService(ApiClient()))
      ..addListener(_onViewModelChanged);
    _viewModel.load();
    _connectNotifications();
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    _reconnectTimer?.cancel();
    _notificationSocket?.close();
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) setState(() {});
  }

  void _reload() => _viewModel.load();

  Future<void> _connectNotifications() async {
    try {
      final socket = await WebSocket.connect(_webSocketUrl());
      _notificationSocket = socket;
      socket.listen(
        _onNotificationMessage,
        onDone: _scheduleNotificationReconnect,
        onError: (_) => _scheduleNotificationReconnect(),
        cancelOnError: true,
      );
    } catch (_) {
      _scheduleNotificationReconnect();
    }
  }

  void _scheduleNotificationReconnect() {
    if (!mounted || (_reconnectTimer?.isActive ?? false)) return;
    _reconnectTimer = Timer(const Duration(seconds: 4), _connectNotifications);
  }

  void _onNotificationMessage(dynamic data) {
    try {
      final decoded = jsonDecode(data.toString()) as Map<String, dynamic>;
      final notification = _RealtimeNotification.fromJson(decoded);
      if (!mounted) return;
      setState(() {
        _realtimeNotifications.insert(0, notification);
        if (_realtimeNotifications.length > 20) {
          _realtimeNotifications.removeRange(20, _realtimeNotifications.length);
        }
        _unreadNotifications++;
      });
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text('${notification.titulo}: ${notification.mensaje}'),
        behavior: SnackBarBehavior.floating,
      ));
    } catch (_) {
      // Ignora mensajes que no correspondan al contrato de notificaciones.
    }
  }

  String _webSocketUrl() {
    final apiUri = Uri.parse(AppConfig.apiBaseUrl);
    return apiUri
        .replace(
          scheme: apiUri.scheme == 'https' ? 'wss' : 'ws',
          path: '${apiUri.path}/ws/notificaciones',
          query: '',
        )
        .toString();
  }

  void _openNotifications(DashboardSummary summary) {
    setState(() => _unreadNotifications = 0);
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (_) => _NotificationsSheet(
        user: widget.user,
        summary: summary,
        realtimeNotifications: _realtimeNotifications,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final summary = _viewModel.summary;
    return Scaffold(
      drawer: _AppDrawer(user: widget.user),
      appBar: AppBar(
        title: const Text('EconoSalud'),
        leading: Builder(
          builder: (context) => IconButton.filledTonal(
            tooltip: 'Menu',
            onPressed: () => Scaffold.of(context).openDrawer(),
            icon: const Icon(Icons.menu_rounded),
          ),
        ),
      ),
      body: _viewModel.loading && summary == null
          ? const Center(child: CircularProgressIndicator())
          : _viewModel.error != null && summary == null
              ? ErrorPanel(message: _viewModel.error!, onRetry: _reload)
              : _DashboardContent(
                  user: widget.user,
                  summary: summary!,
                  onReload: _reload,
                  unreadNotifications: _unreadNotifications,
                  onNotificationsTap: () => _openNotifications(summary)),
    );
  }
}

class _DashboardContent extends StatelessWidget {
  const _DashboardContent(
      {required this.user,
      required this.summary,
      required this.onReload,
      required this.unreadNotifications,
      required this.onNotificationsTap});

  final User user;
  final DashboardSummary summary;
  final VoidCallback onReload;
  final int unreadNotifications;
  final VoidCallback onNotificationsTap;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
      children: [
        _SearchBar(
            user: user,
            summary: summary,
            onReload: onReload,
            unreadNotifications: unreadNotifications,
            onNotificationsTap: onNotificationsTap),
        const SizedBox(height: 10),
        _HeroPanel(user: user, summary: summary),
        const SizedBox(height: 12),
        const _SectionTitle(title: 'Resumen', label: 'Hoy'),
        const SizedBox(height: 8),
        _MetricsGrid(summary: summary),
        const SizedBox(height: 14),
        _RiskCard(summary: summary),
        const SizedBox(height: 18),
        _SectionTitle(
            title: 'Atajos', label: '${_modulesFor(user).length} accesos'),
        const SizedBox(height: 10),
        _ModuleGrid(user: user),
      ],
    );
  }
}

class _SearchBar extends StatelessWidget {
  const _SearchBar(
      {required this.user,
      required this.summary,
      required this.onReload,
      required this.unreadNotifications,
      required this.onNotificationsTap});

  final User user;
  final DashboardSummary summary;
  final VoidCallback onReload;
  final int unreadNotifications;
  final VoidCallback onNotificationsTap;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: InkWell(
            borderRadius: BorderRadius.circular(18),
            onTap: () => _showModuleSearch(context, user),
            child: Container(
              height: 50,
              padding: const EdgeInsets.symmetric(horizontal: 14),
              decoration: BoxDecoration(
                  color: Colors.white, borderRadius: BorderRadius.circular(18)),
              child: const Row(
                children: [
                  Icon(Icons.search_rounded, color: Colors.black38),
                  SizedBox(width: 10),
                  Expanded(
                      child: Text('Buscar modulo o acceso',
                          style: TextStyle(
                              color: Colors.black45,
                              fontWeight: FontWeight.w600))),
                ],
              ),
            ),
          ),
        ),
        const SizedBox(width: 10),
        _SquareButton(
            icon: Icons.notifications_none_rounded,
            badgeCount: unreadNotifications,
            onTap: onNotificationsTap),
        const SizedBox(width: 8),
        _SquareButton(icon: Icons.sync_rounded, onTap: onReload),
      ],
    );
  }

  void _showModuleSearch(BuildContext context, User user) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (_) => _ModuleSearchSheet(user: user),
    );
  }
}

class _HeroPanel extends StatelessWidget {
  const _HeroPanel({required this.user, required this.summary});

  final User user;
  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(26),
        gradient: const LinearGradient(colors: [brandRed, Color(0xFFFF5A4E)]),
        boxShadow: [
          BoxShadow(
              color: brandRed.withValues(alpha: 0.20),
              blurRadius: 24,
              offset: const Offset(0, 12))
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.16),
                    borderRadius: BorderRadius.circular(18)),
                child: const Icon(Icons.local_pharmacy_rounded,
                    color: Colors.white, size: 26),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Hola, ${user.nombreCompleto}',
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: Theme.of(context)
                              .textTheme
                              .titleLarge
                              ?.copyWith(
                                  color: Colors.white,
                                  fontWeight: FontWeight.w900)),
                      const SizedBox(height: 3),
                      Text(user.rolesLabel,
                          style: const TextStyle(
                              color: Colors.white70,
                              fontWeight: FontWeight.w700)),
                    ]),
              ),
              const SizedBox(width: 8),
              const _HeroBadge(label: 'Operativo'),
            ],
          ),
          const SizedBox(height: 14),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _HeroBadge(
                  label: 'S/ ${summary.ventasHoy.toStringAsFixed(2)} hoy'),
              _HeroBadge(label: '${summary.totalProductos} productos'),
              _HeroBadge(label: '${summary.vencidos} vencidos'),
            ],
          ),
        ],
      ),
    );
  }
}

class _MetricsGrid extends StatelessWidget {
  const _MetricsGrid({required this.summary});

  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    final data = [
      _MetricData('Ventas hoy', 'S/ ${summary.ventasHoy.toStringAsFixed(2)}',
          Icons.payments_rounded, brandRed, 'operativo'),
      _MetricData('Tickets', summary.cantidadVentasHoy.toString(),
          Icons.receipt_long_rounded, const Color(0xFF1F5F8B), 'emitidos'),
      _MetricData('Ventas mes', 'S/ ${summary.ventasMes.toStringAsFixed(2)}',
          Icons.show_chart_rounded, const Color(0xFF4B5563), 'acumulado'),
      _MetricData('Compras', 'S/ ${summary.comprasHoy.toStringAsFixed(2)}',
          Icons.inventory_2_rounded, const Color(0xFF8A5A16), 'hoy'),
      _MetricData('Productos', summary.totalProductos.toString(),
          Icons.medication_rounded, brandRed, 'activos'),
      _MetricData('Vencidos', summary.vencidos.toString(),
          Icons.warning_amber_rounded, const Color(0xFF9F1239), 'revision'),
    ];
    final moneyData = [data[0], data[2], data[3]];
    final quickData = [data[1], data[4], data[5], data[3]];

    return Column(children: [
      Card(
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Row(children: [
            SizedBox(
              width: 104,
              height: 104,
              child: CustomPaint(
                painter: _SummaryDonutPainter(moneyData),
                child: Center(
                  child: Text('${summary.cantidadVentasHoy}\ntickets',
                      textAlign: TextAlign.center,
                      style: const TextStyle(fontWeight: FontWeight.w900)),
                ),
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(children: [
                for (final item in moneyData.take(3)) _MetricLine(data: item),
              ]),
            ),
          ]),
        ),
      ),
      const SizedBox(height: 8),
      SizedBox(
        height: 82,
        child: ListView(
          scrollDirection: Axis.horizontal,
          children: [
            for (final item in quickData) _MiniMetric(data: item),
          ],
        ),
      ),
    ]);
  }
}

class _MetricLine extends StatelessWidget {
  const _MetricLine({required this.data});
  final _MetricData data;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 10),
        child: Row(children: [
          CircleAvatar(
            radius: 16,
            backgroundColor: data.color.withValues(alpha: 0.10),
            child: Icon(data.icon, color: data.color, size: 18),
          ),
          const SizedBox(width: 9),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(data.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black54, fontSize: 12)),
              Text(data.value,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w900)),
            ]),
          ),
        ]),
      );
}

class _MiniMetric extends StatelessWidget {
  const _MiniMetric({required this.data});
  final _MetricData data;

  @override
  Widget build(BuildContext context) => Container(
        width: 138,
        margin: const EdgeInsets.only(right: 10),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: data.color.withValues(alpha: 0.10)),
        ),
        child: Row(children: [
          CircleAvatar(
            backgroundColor: data.color.withValues(alpha: 0.10),
            child: Icon(data.icon, color: data.color, size: 20),
          ),
          const SizedBox(width: 9),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(data.value,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w900)),
              Text(data.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black54, fontSize: 12)),
            ]),
          ),
        ]),
      );
}

class _SummaryDonutPainter extends CustomPainter {
  const _SummaryDonutPainter(this.data);
  final List<_MetricData> data;

  @override
  void paint(Canvas canvas, Size size) {
    final values = data.map((item) => _metricNumber(item.value)).toList();
    final total = values.fold<double>(0, (sum, value) => sum + value);
    final stroke = size.width * 0.12;
    final rect = Offset.zero & size;
    final base = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = stroke
      ..strokeCap = StrokeCap.round
      ..color = brandRed.withValues(alpha: 0.08);
    canvas.drawArc(rect.deflate(stroke / 2), -1.57, 6.28, false, base);
    if (total <= 0) return;
    var start = -1.57;
    for (var i = 0; i < data.length; i++) {
      if (values[i] <= 0) continue;
      final sweep = (values[i] / total) * 6.28;
      final paint = Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = stroke
        ..strokeCap = StrokeCap.round
        ..color = data[i].color;
      canvas.drawArc(rect.deflate(stroke / 2), start, sweep, false, paint);
      start += sweep;
    }
  }

  @override
  bool shouldRepaint(covariant _SummaryDonutPainter oldDelegate) =>
      oldDelegate.data != data;
}

double _metricNumber(String value) {
  final cleaned = value.replaceAll('S/', '').replaceAll(',', '').trim();
  return double.tryParse(cleaned) ?? 0;
}

class _RiskCard extends StatelessWidget {
  const _RiskCard({required this.summary});

  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    final items = [
      _RiskData('Stock bajo', summary.stockBajo, const Color(0xFFE67700)),
      _RiskData('Agotados', summary.agotados, const Color(0xFFE03131)),
      _RiskData('Por vencer', summary.porVencer, const Color(0xFF1C7ED6)),
      _RiskData('Vencidos', summary.vencidos, const Color(0xFFC2255C)),
    ];
    final totalRisk = items.fold<int>(0, (sum, item) => sum + item.value);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(children: [
              Expanded(
                child: Text('Control de riesgo',
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.w900)),
              ),
              Chip(
                label: Text(totalRisk == 0 ? 'Estable' : '$totalRisk alertas'),
                backgroundColor: totalRisk == 0
                    ? const Color(0xFFE9FBEF)
                    : brandRed.withValues(alpha: 0.08),
                side: BorderSide.none,
              ),
            ]),
            const SizedBox(height: 14),
            Row(crossAxisAlignment: CrossAxisAlignment.center, children: [
              SizedBox(
                width: 108,
                height: 108,
                child: CustomPaint(
                  painter: _RiskDonutPainter(items),
                  child: Center(
                    child: Column(mainAxisSize: MainAxisSize.min, children: [
                      Text(totalRisk.toString(),
                          style: const TextStyle(
                              fontSize: 24, fontWeight: FontWeight.w900)),
                      const Text('riesgo',
                          style:
                              TextStyle(color: Colors.black45, fontSize: 11)),
                    ]),
                  ),
                ),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  children: [
                    for (final item in items)
                      _RiskRow(
                          label: item.label,
                          value: item.value,
                          total: totalRisk,
                          color: item.color),
                  ],
                ),
              ),
            ]),
          ],
        ),
      ),
    );
  }
}

class _ModuleGrid extends StatelessWidget {
  const _ModuleGrid({required this.user});

  final User user;

  @override
  Widget build(BuildContext context) {
    final modules = _modulesFor(user);
    return LayoutBuilder(
      builder: (context, constraints) {
        final columns = constraints.maxWidth >= 760 ? 3 : 2;
        return GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: modules.length,
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: columns,
            mainAxisSpacing: 10,
            crossAxisSpacing: 10,
            mainAxisExtent: 106,
          ),
          itemBuilder: (_, index) =>
              _ModuleCard(user: user, module: modules[index]),
        );
      },
    );
  }
}

class _ModuleCard extends StatelessWidget {
  const _ModuleCard({required this.user, required this.module});

  final User user;
  final _ModuleItem module;

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(24),
        side:
            BorderSide(color: module.color.withValues(alpha: 0.16), width: 1.2),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(22),
        onTap: () => _openModule(context, module.pageBuilder(user)),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                  color: module.color.withValues(alpha: 0.10),
                  borderRadius: BorderRadius.circular(15)),
              child: Icon(module.icon, color: module.color, size: 24),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(module.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context).textTheme.titleSmall?.copyWith(
                          fontWeight: FontWeight.w900,
                          color: const Color(0xFF12332D))),
                  const SizedBox(height: 4),
                  Text(module.actions.join(' / '),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                          color: Colors.black45,
                          fontSize: 11,
                          fontWeight: FontWeight.w700)),
                ],
              ),
            ),
            Icon(Icons.chevron_right_rounded, color: module.color, size: 24),
          ]),
        ),
      ),
    );
  }
}

class _ModuleSearchSheet extends StatefulWidget {
  const _ModuleSearchSheet({required this.user});
  final User user;

  @override
  State<_ModuleSearchSheet> createState() => _ModuleSearchSheetState();
}

class _ModuleSearchSheetState extends State<_ModuleSearchSheet> {
  String _query = '';

  @override
  Widget build(BuildContext context) {
    final term = _query.trim().toLowerCase();
    final modules = _modulesFor(widget.user).where((module) {
      final haystack =
          '${module.title} ${module.subtitle} ${module.actions.join(' ')}'
              .toLowerCase();
      return term.isEmpty || haystack.contains(term);
    }).toList();
    return SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(
            18, 4, 18, MediaQuery.viewInsetsOf(context).bottom + 18),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          TextField(
            autofocus: true,
            onChanged: (value) => setState(() => _query = value),
            decoration: const InputDecoration(
              prefixIcon: Icon(Icons.search_rounded),
              hintText: 'Buscar ventas, caja, usuarios...',
            ),
          ),
          const SizedBox(height: 12),
          ConstrainedBox(
            constraints: const BoxConstraints(maxHeight: 360),
            child: ListView(
              shrinkWrap: true,
              children: [
                for (final module in modules)
                  Card(
                    child: ListTile(
                      onTap: () {
                        final navigator = Navigator.of(context);
                        navigator.pop();
                        navigator.push(MaterialPageRoute(
                            builder: (_) => module.pageBuilder(widget.user)));
                      },
                      leading: CircleAvatar(
                        backgroundColor: module.color.withValues(alpha: 0.10),
                        child: Icon(module.icon, color: module.color),
                      ),
                      title: Text(module.title,
                          style: const TextStyle(fontWeight: FontWeight.w900)),
                      subtitle: Text(module.subtitle,
                          maxLines: 1, overflow: TextOverflow.ellipsis),
                      trailing: const Icon(Icons.north_east_rounded),
                    ),
                  ),
                if (modules.isEmpty)
                  const Card(
                    child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Text('No se encontraron accesos.'),
                    ),
                  ),
              ],
            ),
          ),
        ]),
      ),
    );
  }
}

class _NotificationsSheet extends StatelessWidget {
  const _NotificationsSheet(
      {required this.user,
      required this.summary,
      required this.realtimeNotifications});
  final User user;
  final DashboardSummary summary;
  final List<_RealtimeNotification> realtimeNotifications;

  @override
  Widget build(BuildContext context) {
    final items = [
      for (final notification in realtimeNotifications)
        _NotificationItem(
            title: notification.titulo,
            subtitle: '${notification.mensaje} - ${notification.fecha}',
            icon: notification.icon,
            color: notification.color),
      ..._notificationItems(user, summary),
    ];
    return SafeArea(
      child: FractionallySizedBox(
        heightFactor: 0.54,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 2, 16, 16),
          child: Column(children: [
            Row(children: [
              const Expanded(
                child: Text('Alertas',
                    style:
                        TextStyle(fontSize: 20, fontWeight: FontWeight.w900)),
              ),
              Chip(
                label: Text('${items.length} mov.'),
                backgroundColor: brandRed.withValues(alpha: 0.08),
                side: BorderSide.none,
              ),
            ]),
            const SizedBox(height: 8),
            Expanded(
              child: ListView.builder(
                padding: EdgeInsets.zero,
                itemCount: items.length,
                itemBuilder: (_, index) {
                  final item = items[index];
                  return Card(
                    child: ListTile(
                      dense: true,
                      contentPadding: const EdgeInsets.symmetric(
                          horizontal: 12, vertical: 4),
                      onTap: item.page == null
                          ? null
                          : () {
                              final navigator = Navigator.of(context);
                              navigator.pop();
                              navigator.push(MaterialPageRoute(
                                  builder: (_) => item.page!));
                            },
                      leading: CircleAvatar(
                        radius: 19,
                        backgroundColor: item.color.withValues(alpha: 0.10),
                        child: Icon(item.icon, color: item.color),
                      ),
                      title: Text(item.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(fontWeight: FontWeight.w900)),
                      subtitle: Text(item.subtitle,
                          maxLines: 1, overflow: TextOverflow.ellipsis),
                      trailing: item.page == null
                          ? null
                          : const Icon(Icons.chevron_right_rounded),
                    ),
                  );
                },
              ),
            ),
          ]),
        ),
      ),
    );
  }
}

class _NotificationItem {
  const _NotificationItem(
      {required this.title,
      required this.subtitle,
      required this.icon,
      required this.color,
      this.page});
  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final Widget? page;
}

class _RealtimeNotification {
  const _RealtimeNotification(
      {required this.tipo,
      required this.titulo,
      required this.mensaje,
      required this.fecha});

  factory _RealtimeNotification.fromJson(Map<String, dynamic> json) {
    return _RealtimeNotification(
      tipo: json['tipo']?.toString() ?? 'INFO',
      titulo: json['titulo']?.toString() ?? 'Notificacion',
      mensaje: json['mensaje']?.toString() ?? '',
      fecha: json['fecha']?.toString() ?? 'Ahora',
    );
  }

  final String tipo;
  final String titulo;
  final String mensaje;
  final String fecha;

  IconData get icon {
    switch (tipo) {
      case 'VENTA':
        return Icons.receipt_long_rounded;
      case 'COMPRA':
        return Icons.inventory_2_rounded;
      case 'INVENTARIO':
        return Icons.medication_rounded;
      default:
        return Icons.notifications_rounded;
    }
  }

  Color get color {
    switch (tipo) {
      case 'VENTA':
        return brandRed;
      case 'COMPRA':
        return const Color(0xFF8A5A16);
      case 'INVENTARIO':
        return const Color(0xFFE67700);
      default:
        return const Color(0xFF1F5F8B);
    }
  }
}

List<_NotificationItem> _notificationItems(
    User user, DashboardSummary summary) {
  final items = <_NotificationItem>[];
  if (user.canViewProductos) {
    if (summary.stockBajo > 0) {
      items.add(_NotificationItem(
          title: 'Stock bajo',
          subtitle: '${summary.stockBajo} productos requieren reposicion',
          icon: Icons.inventory_rounded,
          color: const Color(0xFFE67700),
          page: const ProductsPage()));
    }
    if (summary.vencidos > 0 || summary.porVencer > 0) {
      items.add(_NotificationItem(
          title: 'Vencimientos',
          subtitle:
              '${summary.porVencer} por vencer y ${summary.vencidos} vencidos',
          icon: Icons.warning_amber_rounded,
          color: const Color(0xFFC2255C),
          page: const ProductsPage()));
    }
  }
  if (user.canViewVentas) {
    items.add(_NotificationItem(
        title: 'Ventas de hoy',
        subtitle:
            '${summary.cantidadVentasHoy} tickets por S/ ${summary.ventasHoy.toStringAsFixed(2)}',
        icon: Icons.receipt_long_rounded,
        color: brandRed,
        page: VentasPage(user: user)));
  }
  if (user.canViewCompras) {
    items.add(_NotificationItem(
        title: 'Compras de hoy',
        subtitle:
            'Total registrado S/ ${summary.comprasHoy.toStringAsFixed(2)}',
        icon: Icons.inventory_2_rounded,
        color: const Color(0xFF8A5A16),
        page: const ComprasPage()));
  }
  if (user.canViewReportes) {
    items.add(const _NotificationItem(
        title: 'Reporte listo',
        subtitle: 'Revisa ventas, compras y sesiones de caja',
        icon: Icons.bar_chart_rounded,
        color: Color(0xFF1F5F8B),
        page: ReportesPage()));
  }
  if (items.isEmpty) {
    items.add(const _NotificationItem(
        title: 'Sin alertas pendientes',
        subtitle: 'La operacion se mantiene estable',
        icon: Icons.check_circle_rounded,
        color: Color(0xFF2F9E44)));
  }
  return items;
}

class _AppDrawer extends StatelessWidget {
  const _AppDrawer({required this.user});

  final User user;

  @override
  Widget build(BuildContext context) {
    return Drawer(
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _DrawerHeader(user: user, onTap: () => _showProfile(context)),
              const SizedBox(height: 16),
              _DrawerItem(
                  icon: Icons.dashboard_rounded,
                  label: 'Dashboard',
                  onTap: () => Navigator.of(context).maybePop()),
              for (final module in _modulesFor(user))
                _DrawerItem(
                    icon: module.icon,
                    label: module.title,
                    onTap: () =>
                        _openModule(context, module.pageBuilder(user))),
              const Spacer(),
              _ProfileTile(user: user, onTap: () => _showProfile(context)),
            ],
          ),
        ),
      ),
    );
  }

  void _showProfile(BuildContext context) {
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [
                  CircleAvatar(
                    radius: 28,
                    backgroundColor: brandRed.withValues(alpha: 0.12),
                    child: const Icon(Icons.person_rounded,
                        color: brandRed, size: 30),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                      child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                        Text(user.nombreCompleto,
                            maxLines: 2,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                                fontWeight: FontWeight.w900, fontSize: 18)),
                        Text(user.username,
                            style: const TextStyle(color: Colors.black54)),
                      ])),
                ]),
                const SizedBox(height: 14),
                Wrap(spacing: 8, runSpacing: 8, children: [
                  _ProfileChip(
                      icon: Icons.verified_user_rounded,
                      label: user.rolesLabel),
                  _ProfileChip(icon: Icons.badge_rounded, label: user.username),
                ]),
                const SizedBox(height: 14),
                const _ProfileAction(
                    icon: Icons.touch_app_rounded,
                    title: 'Perfil activo',
                    subtitle:
                        'Toca los accesos del panel o del menu para operar'),
                const SizedBox(height: 10),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton.icon(
                    onPressed: () {
                      Navigator.of(context).pop();
                      _logout(context);
                    },
                    icon: const Icon(Icons.logout_rounded),
                    label: const Text('Cerrar sesion'),
                  ),
                ),
              ]),
        ),
      ),
    );
  }

  Future<void> _logout(BuildContext context) async {
    final navigator = Navigator.of(context);
    final messenger = ScaffoldMessenger.of(context);
    navigator.pop();
    try {
      await AuthService(ApiClient()).logout(user.username);
    } catch (ex) {
      messenger.showSnackBar(const SnackBar(
          content: Text(
              'No se pudo cerrar sesion en el servidor. Se cerrara localmente.')));
    }
    navigator.pushAndRemoveUntil(
        MaterialPageRoute(builder: (_) => const LoginPage()), (route) => false);
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.label});

  final String title;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Text(title,
          style: Theme.of(context)
              .textTheme
              .titleLarge
              ?.copyWith(fontWeight: FontWeight.w900)),
      const Spacer(),
      Chip(
          label: Text(label),
          backgroundColor: Colors.white,
          side: BorderSide.none),
    ]);
  }
}

class _DrawerHeader extends StatelessWidget {
  const _DrawerHeader({required this.user, required this.onTap});

  final User user;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Container(
          width: double.infinity,
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
              gradient:
                  const LinearGradient(colors: [brandRed, Color(0xFFFF5A4E)]),
              borderRadius: BorderRadius.circular(24)),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(children: [
                Container(
                  width: 46,
                  height: 46,
                  decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.18),
                      borderRadius: BorderRadius.circular(16)),
                  child: const Icon(Icons.person_rounded, color: Colors.white),
                ),
                const Spacer(),
                const Icon(Icons.keyboard_arrow_up_rounded,
                    color: Colors.white),
              ]),
              const SizedBox(height: 14),
              Text(user.nombreCompleto,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w900,
                      fontSize: 17)),
              const SizedBox(height: 4),
              Text(user.rolesLabel,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      color: Colors.white70, fontWeight: FontWeight.w700)),
            ],
          ),
        ),
      );
}

class _ProfileTile extends StatelessWidget {
  const _ProfileTile({required this.user, required this.onTap});
  final User user;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => Card(
        color: brandRed.withValues(alpha: 0.08),
        child: ListTile(
          onTap: onTap,
          leading: CircleAvatar(
            backgroundColor: brandRed.withValues(alpha: 0.14),
            child: const Icon(Icons.person_rounded, color: brandRed),
          ),
          title: const Text('Mi perfil',
              style: TextStyle(fontWeight: FontWeight.w900)),
          subtitle:
              Text(user.username, maxLines: 1, overflow: TextOverflow.ellipsis),
          trailing: const Icon(Icons.more_horiz_rounded),
        ),
      );
}

class _ProfileChip extends StatelessWidget {
  const _ProfileChip({required this.icon, required this.label});
  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) => Chip(
        avatar: Icon(icon, size: 17, color: brandRed),
        label: Text(label),
        backgroundColor: brandRed.withValues(alpha: 0.08),
        side: BorderSide.none,
      );
}

class _ProfileAction extends StatelessWidget {
  const _ProfileAction(
      {required this.icon, required this.title, required this.subtitle});
  final IconData icon;
  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: softBackground,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: brandRed.withValues(alpha: 0.10)),
        ),
        child: Row(children: [
          CircleAvatar(
            backgroundColor: brandRed.withValues(alpha: 0.12),
            child: Icon(icon, color: brandRed, size: 20),
          ),
          const SizedBox(width: 10),
          Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                Text(title,
                    style: const TextStyle(fontWeight: FontWeight.w900)),
                Text(subtitle,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style:
                        const TextStyle(color: Colors.black54, fontSize: 12)),
              ])),
        ]),
      );
}

class _HeroBadge extends StatelessWidget {
  const _HeroBadge({required this.label});
  final String label;
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
        decoration: BoxDecoration(
            color: Colors.white.withValues(alpha: 0.16),
            borderRadius: BorderRadius.circular(999)),
        child: Text(label,
            style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w700,
                fontSize: 12)),
      );
}

class _RiskRow extends StatelessWidget {
  const _RiskRow(
      {required this.label,
      required this.value,
      required this.total,
      required this.color});
  final String label;
  final int value;
  final int total;
  final Color color;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 9),
        child: Column(children: [
          Row(children: [
            Container(
                width: 9,
                height: 9,
                decoration:
                    BoxDecoration(color: color, shape: BoxShape.circle)),
            const SizedBox(width: 8),
            Expanded(
                child: Text(label,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(fontSize: 12))),
            Text(value.toString(),
                style:
                    const TextStyle(fontWeight: FontWeight.w900, fontSize: 12)),
          ]),
          const SizedBox(height: 4),
          ClipRRect(
            borderRadius: BorderRadius.circular(99),
            child: LinearProgressIndicator(
              minHeight: 5,
              value: total == 0 ? 0 : value / total,
              color: color,
              backgroundColor: color.withValues(alpha: 0.10),
            ),
          ),
        ]),
      );
}

class _RiskData {
  const _RiskData(this.label, this.value, this.color);
  final String label;
  final int value;
  final Color color;
}

class _RiskDonutPainter extends CustomPainter {
  const _RiskDonutPainter(this.items);
  final List<_RiskData> items;

  @override
  void paint(Canvas canvas, Size size) {
    final stroke = size.width * 0.12;
    final rect = Offset.zero & size;
    final total = items.fold<int>(0, (sum, item) => sum + item.value);
    final basePaint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = stroke
      ..strokeCap = StrokeCap.round
      ..color = brandRed.withValues(alpha: 0.08);
    canvas.drawArc(rect.deflate(stroke / 2), -1.57, 6.28, false, basePaint);
    if (total == 0) return;
    var start = -1.57;
    for (final item in items.where((item) => item.value > 0)) {
      final sweep = (item.value / total) * 6.28;
      final paint = Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = stroke
        ..strokeCap = StrokeCap.round
        ..color = item.color;
      canvas.drawArc(rect.deflate(stroke / 2), start, sweep, false, paint);
      start += sweep;
    }
  }

  @override
  bool shouldRepaint(covariant _RiskDonutPainter oldDelegate) =>
      oldDelegate.items != items;
}

class _SquareButton extends StatelessWidget {
  const _SquareButton(
      {required this.icon, required this.onTap, this.badgeCount = 0});
  final IconData icon;
  final VoidCallback onTap;
  final int badgeCount;
  @override
  Widget build(BuildContext context) => Material(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        child: InkWell(
            borderRadius: BorderRadius.circular(18),
            onTap: onTap,
            child: SizedBox(
              width: 50,
              height: 50,
              child: Stack(alignment: Alignment.center, children: [
                Icon(icon),
                if (badgeCount > 0)
                  Positioned(
                    top: 8,
                    right: 8,
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 5, vertical: 2),
                      decoration: BoxDecoration(
                        color: brandRed,
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(
                        badgeCount > 9 ? '9+' : '$badgeCount',
                        style: const TextStyle(
                            color: Colors.white,
                            fontSize: 10,
                            fontWeight: FontWeight.w900),
                      ),
                    ),
                  ),
              ]),
            )),
      );
}

class _DrawerItem extends StatelessWidget {
  const _DrawerItem(
      {required this.icon, required this.label, required this.onTap});
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    const color = brandDark;
    return ListTile(
      leading: Icon(icon, color: color),
      title: Text(label,
          style: const TextStyle(color: color, fontWeight: FontWeight.w700)),
      onTap: onTap,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
    );
  }
}

class _MetricData {
  const _MetricData(this.title, this.value, this.icon, this.color, this.hint);
  final String title;
  final String value;
  final IconData icon;
  final Color color;
  final String hint;
}

class _ModuleItem {
  const _ModuleItem(this.title, this.subtitle, this.icon, this.color,
      this.pageBuilder, this.actions);
  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final Widget Function(User user) pageBuilder;
  final List<String> actions;
}

List<_ModuleItem> _modulesFor(User user) => [
      if (user.canViewProductos)
        _ModuleItem(
            'Productos',
            'Inventario, stock y vencimientos',
            Icons.medication_rounded,
            brandRed,
            (_) => const ProductsPage(),
            ['Buscar', 'Stock']),
      if (user.canViewVentas)
        _ModuleItem(
            'Ventas',
            'Atencion, comprobante y ultimas ventas',
            Icons.point_of_sale_rounded,
            brandRed,
            (user) => VentasPage(user: user),
            ['Carrito', 'Cobrar']),
      if (user.canViewCompras)
        _ModuleItem(
            'Compras',
            'Proveedores e ingresos',
            Icons.inventory_2_rounded,
            brandRed,
            (_) => const ComprasPage(),
            ['Proveedor', 'Ingreso']),
      if (user.canViewCaja)
        _ModuleItem(
            'Caja',
            'Apertura, cierre y control diario',
            Icons.account_balance_wallet_rounded,
            brandRed,
            (user) => CajaPage(user: user),
            ['Abrir', 'Cerrar']),
      if (user.canViewReportes)
        _ModuleItem(
            'Reportes',
            'Ventas por rango y productos top',
            Icons.bar_chart_rounded,
            brandRed,
            (_) => const ReportesPage(),
            ['Rangos', 'Top']),
      if (user.canViewUsuarios)
        _ModuleItem(
            'Usuarios',
            'Roles, perfiles y accesos',
            Icons.group_rounded,
            brandRed,
            (_) => const UsuariosPage(),
            ['Roles', 'Acceso']),
    ];

Future<void> _openModule(BuildContext context, Widget page) =>
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => page));
