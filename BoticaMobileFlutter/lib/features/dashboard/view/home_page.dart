import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';

import '../../../app.dart';
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
import '../../usuarios/model/admin_user.dart';
import '../../usuarios/service/user_admin_service.dart';
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
  final _scaffoldKey = GlobalKey<ScaffoldState>();
  late final DashboardViewModel _viewModel;
  late User _currentUser;
  WebSocket? _notificationSocket;
  Timer? _reconnectTimer;
  final List<_RealtimeNotification> _realtimeNotifications = [];
  int _unreadNotifications = 0;

  @override
  void initState() {
    super.initState();
    _currentUser = widget.user;
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
      if (AppPreferencesController.notificationsEnabled.value) {
        _showNotificationToast(notification);
      }
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
        user: _currentUser,
        summary: summary,
        realtimeNotifications: _realtimeNotifications,
      ),
    );
  }

  void _showNotificationToast(_RealtimeNotification notification) {
    final summary = _viewModel.summary;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      elevation: 0,
      duration: const Duration(seconds: 4),
      backgroundColor: Colors.transparent,
      behavior: SnackBarBehavior.floating,
      margin: const EdgeInsets.fromLTRB(14, 0, 14, 16),
      content: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: notification.color.withValues(alpha: 0.16)),
          boxShadow: [
            BoxShadow(
              color: brandDark.withValues(alpha: 0.14),
              blurRadius: 22,
              offset: const Offset(0, 10),
            )
          ],
        ),
        child: Row(children: [
          CircleAvatar(
            backgroundColor: notification.color.withValues(alpha: 0.12),
            child: Icon(notification.icon, color: notification.color),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(notification.titulo,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        color: brandDark, fontWeight: FontWeight.w900)),
                Text(notification.mensaje,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(color: Colors.black54)),
              ],
            ),
          ),
          if (summary != null)
            TextButton(
              onPressed: () {
                ScaffoldMessenger.of(context).hideCurrentSnackBar();
                _openNotifications(summary);
              },
              child: const Text('Ver'),
            ),
        ]),
      ),
    ));
  }

  @override
  Widget build(BuildContext context) {
    final summary = _viewModel.summary;
    return Scaffold(
      key: _scaffoldKey,
      drawer: _AppDrawer(
        user: _currentUser,
        onUserUpdated: (user) => setState(() => _currentUser = user),
      ),
      appBar: AppBar(
        title: const _BrandTitle(),
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
                  user: _currentUser,
                  summary: summary!,
                  onReload: _reload,
                  unreadNotifications: _unreadNotifications,
                  onNotificationsTap: () => _openNotifications(summary)),
      bottomNavigationBar: summary == null
          ? null
          : AppQuickNavBar(
              current: 'inicio',
              alerts: _unreadNotifications,
              onHome: () {},
              onAlerts: () => _openNotifications(summary),
              onCaja: () {
                if (_currentUser.canViewCaja) {
                  Navigator.of(context).push(MaterialPageRoute(
                      builder: (_) => CajaPage(user: _currentUser)));
                }
              },
              onProfile: () => _AppDrawer.showProfileSheet(
                context,
                user: _currentUser,
                onUserUpdated: (user) => setState(() => _currentUser = user),
              ),
            ),
    );
  }
}

class _BrandTitle extends StatelessWidget {
  const _BrandTitle();

  @override
  Widget build(BuildContext context) {
    final dark = isDarkMode(context);
    return Row(mainAxisSize: MainAxisSize.min, children: [
      Container(
        width: 8,
        height: 28,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(99),
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [brandSky, brandSkyDark],
          ),
        ),
      ),
      const SizedBox(width: 9),
      RichText(
        text: TextSpan(
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                height: 1,
                letterSpacing: -0.7,
                fontWeight: FontWeight.w900,
              ),
          children: [
            TextSpan(
              text: 'Econo',
              style:
                  TextStyle(color: dark ? const Color(0xFFEAF8FF) : brandDark),
            ),
            const TextSpan(
              text: 'Salud',
              style: TextStyle(color: brandSkyDark),
            ),
          ],
        ),
      ),
    ]);
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
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 104),
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
    final dark = isDarkMode(context);
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
                  color: dark ? darkSurfaceSoft : Colors.white,
                  borderRadius: BorderRadius.circular(18)),
              child: Row(
                children: [
                  Icon(Icons.search_rounded,
                      color: dark ? const Color(0xFFB7CFDA) : Colors.black38),
                  const SizedBox(width: 10),
                  Expanded(
                      child: Text('Buscar modulo o acceso',
                          style: TextStyle(
                              color: dark
                                  ? const Color(0xFFB7CFDA)
                                  : Colors.black45,
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
    final dark = isDarkMode(context);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(26),
        gradient: LinearGradient(
            colors: dark
                ? const [Color(0xFF0B7896), Color(0xFF38BDF8)]
                : const [brandRed, Color(0xFFFF5A4E)]),
        boxShadow: [
          BoxShadow(
              color: (dark ? brandSkyDark : brandRed).withValues(alpha: 0.20),
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
                  style: TextStyle(
                      color: moduleTextSecondary(context), fontSize: 12)),
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
          color: moduleSurface(context),
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
                  style: TextStyle(
                      color: moduleTextSecondary(context), fontSize: 12)),
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
          page: ProductsPage(user: user)));
    }
    if (summary.vencidos > 0 || summary.porVencer > 0) {
      items.add(_NotificationItem(
          title: 'Vencimientos',
          subtitle:
              '${summary.porVencer} por vencer y ${summary.vencidos} vencidos',
          icon: Icons.warning_amber_rounded,
          color: const Color(0xFFC2255C),
          page: ProductsPage(user: user)));
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
        page: ComprasPage(user: user)));
  }
  if (user.canViewReportes) {
    items.add(_NotificationItem(
        title: 'Reporte listo',
        subtitle: 'Revisa ventas, compras y sesiones de caja',
        icon: Icons.bar_chart_rounded,
        color: const Color(0xFF1F5F8B),
        page: ReportesPage(user: user)));
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
  const _AppDrawer({required this.user, required this.onUserUpdated});

  final User user;
  final ValueChanged<User> onUserUpdated;

  @override
  Widget build(BuildContext context) {
    return Drawer(
      backgroundColor: isDarkMode(context) ? const Color(0xFF111D22) : null,
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _DrawerHeader(
                  user: user,
                  onTap: () => showProfileSheet(context,
                      user: user, onUserUpdated: onUserUpdated)),
              const SizedBox(height: 12),
              Expanded(
                child: ListView(
                  padding: EdgeInsets.zero,
                  children: [
                    const _DrawerSectionLabel('Principal'),
                    _DrawerItem(
                        icon: Icons.dashboard_rounded,
                        label: 'Dashboard',
                        onTap: () => Navigator.of(context).maybePop()),
                    const SizedBox(height: 8),
                    const _DrawerSectionLabel('Modulos'),
                    for (final module in _modulesFor(user))
                      _DrawerItem(
                          icon: module.icon,
                          label: module.title,
                          onTap: () =>
                              _openModule(context, module.pageBuilder(user))),
                  ],
                ),
              ),
              const SizedBox(height: 12),
              _ProfileTile(
                  user: user,
                  onTap: () => showProfileSheet(context,
                      user: user, onUserUpdated: onUserUpdated)),
            ],
          ),
        ),
      ),
    );
  }

  static void showProfileSheet(
    BuildContext context, {
    required User user,
    required ValueChanged<User> onUserUpdated,
  }) {
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      builder: (_) => SafeArea(
        child: FractionallySizedBox(
          heightFactor: 0.82,
          child: ListView(
            padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
            children: [
              const Text('Perfil',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.w900)),
              const SizedBox(height: 18),
              Column(children: [
                CircleAvatar(
                  radius: 38,
                  backgroundColor: brandSky.withValues(alpha: 0.16),
                  child: const Icon(Icons.person_rounded,
                      color: brandSkyDark, size: 40),
                ),
                const SizedBox(height: 10),
                Text(user.nombreCompleto,
                    textAlign: TextAlign.center,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        fontSize: 18, fontWeight: FontWeight.w900)),
                Text(user.username,
                    style: const TextStyle(color: Colors.black54)),
                const SizedBox(height: 8),
                Wrap(spacing: 8, runSpacing: 8, children: [
                  _ProfileChip(
                      icon: Icons.verified_user_rounded,
                      label: user.rolesLabel),
                  _ProfileChip(icon: Icons.badge_rounded, label: user.username),
                ]),
              ]),
              const SizedBox(height: 18),
              _ProfileMenuSection(title: 'Cuenta', children: [
                _ProfileMenuItem(
                  icon: Icons.manage_accounts_rounded,
                  title: 'Administrar perfil',
                  onTap: () => _openEditProfile(context, user, onUserUpdated),
                ),
                _ProfileMenuItem(
                  icon: Icons.lock_reset_rounded,
                  title: 'Cambiar contrasena',
                  onTap: () => _openChangePassword(context, user),
                ),
                _ProfileMenuItem(
                  icon: Icons.security_rounded,
                  title: 'Seguridad y privacidad',
                  onTap: () => _openInfoPage(
                    context,
                    title: 'Seguridad y privacidad',
                    icon: Icons.security_rounded,
                    description:
                        'Tu sesion usa autenticacion del servidor y los cambios sensibles, como contrasena, se aplican mediante el modulo de usuarios.',
                    bullets: const [
                      'Cambia tu contrasena periodicamente.',
                      'No compartas tu usuario con otros operadores.',
                      'Cierra sesion al terminar tu turno.',
                    ],
                  ),
                ),
              ]),
              _ProfileMenuSection(title: 'Preferencias', children: [
                _ProfileMenuItem(
                  icon: Icons.notifications_none_rounded,
                  title: 'Notificaciones',
                  trailing: ValueListenableBuilder<bool>(
                    valueListenable:
                        AppPreferencesController.notificationsEnabled,
                    builder: (_, enabled, __) => Switch(
                      value: enabled,
                      onChanged: (value) => AppPreferencesController
                          .notificationsEnabled.value = value,
                    ),
                  ),
                ),
                _ProfileMenuItem(
                  icon: Icons.dark_mode_outlined,
                  title: 'Modo oscuro',
                  trailing: ValueListenableBuilder<ThemeMode>(
                    valueListenable: AppThemeController.mode,
                    builder: (_, mode, __) => Switch(
                      value: mode == ThemeMode.dark,
                      onChanged: AppThemeController.setDark,
                    ),
                  ),
                ),
                _ProfileMenuItem(
                  icon: Icons.translate_rounded,
                  title: 'Idioma',
                  value: AppPreferencesController.language.value,
                  onTap: () => _selectLanguage(context),
                ),
              ]),
              _ProfileMenuSection(title: 'Soporte', children: [
                _ProfileMenuItem(
                  icon: Icons.help_outline_rounded,
                  title: 'Centro de ayuda',
                  onTap: () => _openInfoPage(
                    context,
                    title: 'Centro de ayuda',
                    icon: Icons.help_outline_rounded,
                    description:
                        'Accesos rapidos para operar EconoSalud desde el movil.',
                    bullets: const [
                      'Productos: busca inventario y ajusta stock desde los tres puntos.',
                      'Compras: registra ingresos y actualiza el historial.',
                      'Caja: controla apertura, cierre y resumen diario.',
                    ],
                  ),
                ),
                _ProfileMenuItem(
                  icon: Icons.description_outlined,
                  title: 'Terminos y politicas',
                  onTap: () => _openInfoPage(
                    context,
                    title: 'Terminos y politicas',
                    icon: Icons.description_outlined,
                    description:
                        'El uso del sistema queda reservado para personal autorizado de la botica.',
                    bullets: const [
                      'Toda venta, compra y ajuste queda registrado.',
                      'Los datos deben corresponder a operaciones reales.',
                      'El acceso es personal y no transferible.',
                    ],
                  ),
                ),
                _ProfileMenuItem(
                  icon: Icons.info_outline_rounded,
                  title: 'Acerca de EconoSalud',
                  value: 'v1.0',
                  onTap: () => _openInfoPage(
                    context,
                    title: 'Acerca de EconoSalud',
                    icon: Icons.info_outline_rounded,
                    description:
                        'Botica movil conectada a BoticaAPIREST, RMI y MySQL para operaciones distribuidas.',
                    bullets: const [
                      'Version movil: 1.0',
                      'Notificaciones en tiempo real por WebSocket.',
                      'Modulos: dashboard, productos, ventas, compras, caja y reportes.',
                    ],
                  ),
                ),
              ]),
              const SizedBox(height: 8),
              FilledButton.icon(
                onPressed: () {
                  Navigator.of(context).pop();
                  _logout(context, user);
                },
                icon: const Icon(Icons.logout_rounded),
                label: const Text('Cerrar sesion'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  static void _showProfileNotice(BuildContext context, String option) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      elevation: 0,
      backgroundColor: Colors.transparent,
      behavior: SnackBarBehavior.floating,
      content: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [
            BoxShadow(
              color: brandDark.withValues(alpha: 0.12),
              blurRadius: 18,
              offset: const Offset(0, 8),
            )
          ],
        ),
        child: Text('$option estara disponible proximamente.',
            style:
                const TextStyle(color: brandDark, fontWeight: FontWeight.w800)),
      ),
    ));
  }

  static Future<void> _openEditProfile(
      BuildContext context, User user, ValueChanged<User> onUserUpdated) async {
    Navigator.of(context).pop();
    final updated = await Navigator.of(context).push<User>(MaterialPageRoute(
      builder: (_) => EditProfilePage(user: user),
    ));
    if (updated != null) {
      onUserUpdated(updated);
    }
  }

  static Future<void> _openChangePassword(
      BuildContext context, User user) async {
    Navigator.of(context).pop();
    await Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => ChangePasswordPage(user: user),
    ));
  }

  static Future<void> _selectLanguage(BuildContext context) async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      showDragHandle: true,
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 6, 20, 24),
          child: Column(mainAxisSize: MainAxisSize.min, children: [
            const ListTile(
              leading: Icon(Icons.translate_rounded),
              title: Text('Seleccionar idioma',
                  style: TextStyle(fontWeight: FontWeight.w900)),
            ),
            for (final option in const ['Espanol', 'Ingles'])
              ListTile(
                onTap: () => Navigator.of(context).pop(option),
                title: Text(option),
                trailing: AppPreferencesController.language.value == option
                    ? const Icon(Icons.check_circle_rounded,
                        color: brandSkyDark)
                    : const Icon(Icons.circle_outlined),
              ),
          ]),
        ),
      ),
    );
    if (selected != null) {
      AppPreferencesController.language.value = selected;
      if (context.mounted) {
        _showProfileNotice(context, 'Idioma cambiado a $selected');
      }
    }
  }

  static void _openInfoPage(
    BuildContext context, {
    required String title,
    required IconData icon,
    required String description,
    required List<String> bullets,
  }) {
    Navigator.of(context).pop();
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => ProfileInfoPage(
        title: title,
        icon: icon,
        description: description,
        bullets: bullets,
      ),
    ));
  }

  static Future<void> _logout(BuildContext context, User user) async {
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

class EditProfilePage extends StatefulWidget {
  const EditProfilePage({required this.user, super.key});

  final User user;

  @override
  State<EditProfilePage> createState() => _EditProfilePageState();
}

class _EditProfilePageState extends State<EditProfilePage> {
  final _formKey = GlobalKey<FormState>();
  final _username = TextEditingController();
  final _email = TextEditingController();
  final _name = TextEditingController();
  final _dni = TextEditingController();
  final _phone = TextEditingController();
  late final UserAdminService _service;
  AdminUser? _adminUser;
  bool _loading = true;
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _service = UserAdminService(ApiClient());
    _load();
  }

  @override
  void dispose() {
    _username.dispose();
    _email.dispose();
    _name.dispose();
    _dni.dispose();
    _phone.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final user = await _service.userById(widget.user.id);
      if (!mounted) return;
      _adminUser = user;
      _username.text = user.username;
      _email.text = user.email;
      _name.text = user.nombreCompleto;
      _dni.text = user.dni;
      _phone.text = user.telefono;
    } catch (ex) {
      if (!mounted) return;
      _error = ex.toString().replaceFirst('Exception: ', '');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _save() async {
    final user = _adminUser;
    if (user == null || !_formKey.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final roleId = user.roles.isNotEmpty
          ? user.roles.first.id
          : (widget.user.roles.isNotEmpty ? widget.user.roles.first.id : 1);
      await _service.update(
        user,
        username: _username.text.trim(),
        email: _email.text.trim(),
        nombreCompleto: _name.text.trim(),
        dni: _dni.text.trim(),
        telefono: _phone.text.trim(),
        roleId: roleId,
        activo: user.activo,
      );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Perfil actualizado correctamente'),
        behavior: SnackBarBehavior.floating,
      ));
      Navigator.of(context).pop(User(
        id: widget.user.id,
        username: _username.text.trim(),
        nombreCompleto: _name.text.trim(),
        roles: widget.user.roles,
      ));
    } catch (ex) {
      if (mounted) {
        setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Administrar perfil')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
                children: [
                  const CompactModuleHeader(
                    title: 'Datos de usuario',
                    subtitle: 'Actualiza tu informacion personal y de acceso',
                    icon: Icons.manage_accounts_rounded,
                    color: brandSkyDark,
                  ),
                  const SizedBox(height: 12),
                  if (_error != null)
                    ErrorPanel(
                        message: _error!,
                        onRetry: () => setState(() => _error = null)),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Form(
                        key: _formKey,
                        child: Column(children: [
                          TextFormField(
                            controller: _name,
                            decoration: const InputDecoration(
                              labelText: 'Nombre completo',
                              prefixIcon: Icon(Icons.person_outline_rounded),
                            ),
                            validator: (value) =>
                                value == null || value.trim().isEmpty
                                    ? 'Ingrese nombre completo'
                                    : null,
                          ),
                          const SizedBox(height: 12),
                          TextFormField(
                            controller: _username,
                            decoration: const InputDecoration(
                              labelText: 'Usuario',
                              prefixIcon: Icon(Icons.alternate_email_rounded),
                            ),
                            validator: (value) =>
                                value == null || value.trim().isEmpty
                                    ? 'Ingrese usuario'
                                    : null,
                          ),
                          const SizedBox(height: 12),
                          TextFormField(
                            controller: _email,
                            keyboardType: TextInputType.emailAddress,
                            decoration: const InputDecoration(
                              labelText: 'Correo',
                              prefixIcon: Icon(Icons.mail_outline_rounded),
                            ),
                          ),
                          const SizedBox(height: 12),
                          Row(children: [
                            Expanded(
                              child: TextFormField(
                                controller: _dni,
                                keyboardType: TextInputType.number,
                                decoration:
                                    const InputDecoration(labelText: 'DNI'),
                              ),
                            ),
                            const SizedBox(width: 10),
                            Expanded(
                              child: TextFormField(
                                controller: _phone,
                                keyboardType: TextInputType.phone,
                                decoration: const InputDecoration(
                                    labelText: 'Telefono'),
                              ),
                            ),
                          ]),
                          const SizedBox(height: 16),
                          FilledButton.icon(
                            onPressed: _saving ? null : _save,
                            icon: _saving
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2),
                                  )
                                : const Icon(Icons.save_rounded),
                            label: const Text('Guardar cambios'),
                          ),
                        ]),
                      ),
                    ),
                  ),
                ],
              ),
      );
}

class ChangePasswordPage extends StatefulWidget {
  const ChangePasswordPage({required this.user, super.key});

  final User user;

  @override
  State<ChangePasswordPage> createState() => _ChangePasswordPageState();
}

class _ChangePasswordPageState extends State<ChangePasswordPage> {
  final _formKey = GlobalKey<FormState>();
  final _password = TextEditingController();
  final _confirm = TextEditingController();
  late final UserAdminService _service;
  AdminUser? _adminUser;
  bool _loading = true;
  bool _saving = false;
  bool _hidePassword = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _service = UserAdminService(ApiClient());
    _load();
  }

  @override
  void dispose() {
    _password.dispose();
    _confirm.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      _adminUser = await _service.userById(widget.user.id);
    } catch (ex) {
      _error = ex.toString().replaceFirst('Exception: ', '');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _save() async {
    final user = _adminUser;
    if (user == null || !_formKey.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await _service.changePassword(user, _password.text);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Contrasena actualizada correctamente'),
        behavior: SnackBarBehavior.floating,
      ));
      Navigator.of(context).pop();
    } catch (ex) {
      if (mounted) {
        setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Cambiar contrasena')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
                children: [
                  const CompactModuleHeader(
                    title: 'Nueva contrasena',
                    subtitle:
                        'Usa al menos 6 caracteres para actualizar tu acceso',
                    icon: Icons.lock_reset_rounded,
                    color: brandSkyDark,
                  ),
                  const SizedBox(height: 12),
                  if (_error != null)
                    ErrorPanel(
                        message: _error!,
                        onRetry: () => setState(() => _error = null)),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Form(
                        key: _formKey,
                        child: Column(children: [
                          TextFormField(
                            controller: _password,
                            obscureText: _hidePassword,
                            decoration: InputDecoration(
                              labelText: 'Nueva contrasena',
                              prefixIcon:
                                  const Icon(Icons.lock_outline_rounded),
                              suffixIcon: IconButton(
                                onPressed: () => setState(
                                    () => _hidePassword = !_hidePassword),
                                icon: Icon(_hidePassword
                                    ? Icons.visibility_off_rounded
                                    : Icons.visibility_rounded),
                              ),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Ingrese contrasena';
                              }
                              if (value.length < 6) {
                                return 'Minimo 6 caracteres';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 12),
                          TextFormField(
                            controller: _confirm,
                            obscureText: _hidePassword,
                            decoration: const InputDecoration(
                              labelText: 'Confirmar contrasena',
                              prefixIcon: Icon(Icons.verified_user_outlined),
                            ),
                            validator: (value) => value != _password.text
                                ? 'Las contrasenas no coinciden'
                                : null,
                          ),
                          const SizedBox(height: 16),
                          FilledButton.icon(
                            onPressed: _saving ? null : _save,
                            icon: _saving
                                ? const SizedBox(
                                    width: 18,
                                    height: 18,
                                    child: CircularProgressIndicator(
                                        strokeWidth: 2),
                                  )
                                : const Icon(Icons.check_rounded),
                            label: const Text('Actualizar contrasena'),
                          ),
                        ]),
                      ),
                    ),
                  ),
                ],
              ),
      );
}

class ProfileInfoPage extends StatelessWidget {
  const ProfileInfoPage({
    required this.title,
    required this.icon,
    required this.description,
    required this.bullets,
    super.key,
  });

  final String title;
  final IconData icon;
  final String description;
  final List<String> bullets;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(title)),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
          children: [
            CompactModuleHeader(
              title: title,
              subtitle: description,
              icon: icon,
              color: brandSkyDark,
            ),
            const SizedBox(height: 12),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text('Detalles',
                        style: TextStyle(
                            fontSize: 18, fontWeight: FontWeight.w900)),
                    const SizedBox(height: 10),
                    for (final bullet in bullets)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 10),
                        child: Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Icon(Icons.check_circle_rounded,
                                color: brandSkyDark, size: 20),
                            const SizedBox(width: 10),
                            Expanded(child: Text(bullet)),
                          ],
                        ),
                      ),
                  ],
                ),
              ),
            ),
          ],
        ),
      );
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
              gradient: const LinearGradient(
                  colors: [brandSkyDark, Color(0xFF38BDF8)]),
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
  Widget build(BuildContext context) => Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(24),
          child: Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: isDarkMode(context)
                  ? darkSurfaceSoft
                  : brandSky.withValues(alpha: 0.09),
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: brandSky.withValues(alpha: 0.14)),
              boxShadow: [
                BoxShadow(
                  color: brandDark.withValues(
                      alpha: isDarkMode(context) ? 0.22 : 0.07),
                  blurRadius: 18,
                  offset: const Offset(0, 8),
                )
              ],
            ),
            child: Row(children: [
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  gradient:
                      const LinearGradient(colors: [brandSky, brandSkyDark]),
                  borderRadius: BorderRadius.circular(18),
                ),
                child: const Icon(Icons.person_rounded,
                    color: Colors.white, size: 28),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Mi perfil',
                        style: TextStyle(
                            color: moduleTextPrimary(context),
                            fontWeight: FontWeight.w900,
                            fontSize: 16)),
                    const SizedBox(height: 2),
                    Text(user.username,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(color: moduleTextSecondary(context))),
                    const SizedBox(height: 7),
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: brandSky.withValues(alpha: 0.12),
                        borderRadius: BorderRadius.circular(999),
                      ),
                      child: Text(user.rolesLabel,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(
                              color: brandSkyDark,
                              fontSize: 11,
                              fontWeight: FontWeight.w900)),
                    ),
                  ],
                ),
              ),
              Container(
                width: 34,
                height: 34,
                decoration: BoxDecoration(
                  color: moduleSurface(context),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: const Icon(Icons.arrow_forward_ios_rounded,
                    size: 15, color: brandSkyDark),
              ),
            ]),
          ),
        ),
      );
}

class _ProfileChip extends StatelessWidget {
  const _ProfileChip({required this.icon, required this.label});
  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) => Chip(
        avatar: Icon(icon, size: 17, color: brandSkyDark),
        label: Text(label),
        backgroundColor: brandSky.withValues(alpha: 0.10),
        side: BorderSide.none,
      );
}

class _ProfileMenuSection extends StatelessWidget {
  const _ProfileMenuSection({required this.title, required this.children});

  final String title;
  final List<Widget> children;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Padding(
            padding: const EdgeInsets.only(left: 4, bottom: 7),
            child: Text(title,
                style: const TextStyle(
                    color: Colors.black54,
                    fontSize: 12,
                    fontWeight: FontWeight.w900)),
          ),
          Container(
            decoration: BoxDecoration(
              color: moduleSurface(context),
              borderRadius: BorderRadius.circular(18),
              boxShadow: [
                BoxShadow(
                  color: brandDark.withValues(alpha: 0.04),
                  blurRadius: 16,
                  offset: const Offset(0, 8),
                )
              ],
            ),
            child: Column(children: children),
          ),
        ]),
      );
}

class _ProfileMenuItem extends StatelessWidget {
  const _ProfileMenuItem({
    required this.icon,
    required this.title,
    this.onTap,
    this.value,
    this.trailing,
  });

  final IconData icon;
  final String title;
  final VoidCallback? onTap;
  final String? value;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 13),
          child: Row(children: [
            Icon(icon, color: moduleTextSecondary(context), size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Text(title,
                  style: TextStyle(
                      color: moduleTextPrimary(context),
                      fontWeight: FontWeight.w800)),
            ),
            if (value != null)
              Padding(
                padding: const EdgeInsets.only(right: 8),
                child: Text(value!,
                    style: TextStyle(
                        color: moduleTextMuted(context), fontSize: 12)),
              ),
            trailing ?? const Icon(Icons.chevron_right_rounded, size: 20),
          ]),
        ),
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
        color: isDarkMode(context) ? darkSurfaceSoft : Colors.white,
        borderRadius: BorderRadius.circular(18),
        child: InkWell(
            borderRadius: BorderRadius.circular(18),
            onTap: onTap,
            child: SizedBox(
              width: 50,
              height: 50,
              child: Stack(alignment: Alignment.center, children: [
                Icon(icon, color: moduleTextPrimary(context)),
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
    final color = moduleTextPrimary(context);
    final accent = isDarkMode(context) ? brandSky : brandSkyDark;
    return Padding(
      padding: const EdgeInsets.only(bottom: 7),
      child: Material(
        color: moduleSurface(context)
            .withValues(alpha: isDarkMode(context) ? 0.78 : 0.72),
        borderRadius: BorderRadius.circular(18),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(18),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 9),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: accent.withValues(alpha: 0.08)),
            ),
            child: Row(children: [
              Container(
                width: 36,
                height: 36,
                decoration: BoxDecoration(
                  color: accent.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(icon, color: accent, size: 21),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Text(label,
                    style: TextStyle(
                        color: color,
                        fontSize: 15,
                        fontWeight: FontWeight.w900)),
              ),
              Icon(Icons.chevron_right_rounded,
                  color: moduleTextMuted(context), size: 20),
            ]),
          ),
        ),
      ),
    );
  }
}

class _DrawerSectionLabel extends StatelessWidget {
  const _DrawerSectionLabel(this.text);

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(4, 0, 4, 8),
        child: Row(children: [
          Text(text.toUpperCase(),
              style: TextStyle(
                  color: moduleTextMuted(context),
                  fontSize: 11,
                  fontWeight: FontWeight.w900,
                  letterSpacing: 0.9)),
          const SizedBox(width: 10),
          Expanded(
            child: Container(
              height: 1,
              color: moduleTextMuted(context).withValues(alpha: 0.22),
            ),
          ),
        ]),
      );
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
            (user) => ProductsPage(user: user),
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
            (user) => ComprasPage(user: user),
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
            (user) => ReportesPage(user: user),
            ['Rangos', 'Top']),
      if (user.canViewUsuarios)
        _ModuleItem(
            'Usuarios',
            'Roles, perfiles y accesos',
            Icons.group_rounded,
            brandRed,
            (user) => UsuariosPage(user: user),
            ['Roles', 'Acceso']),
    ];

Future<void> _openModule(BuildContext context, Widget page) =>
    Navigator.of(context).push(MaterialPageRoute(builder: (_) => page));
