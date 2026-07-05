import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../../auth/model/user.dart';
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

  @override
  void initState() {
    super.initState();
    _viewModel = DashboardViewModel(DashboardService(ApiClient()))..addListener(_onViewModelChanged);
    _viewModel.load();
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) {
      setState(() {});
    }
  }

  void _reload() {
    _viewModel.load();
  }

  @override
  Widget build(BuildContext context) {
    final summary = _viewModel.summary;

    return Scaffold(
      appBar: AppBar(
        title: const Text('EconoSalud'),
        actions: [
          IconButton(onPressed: _reload, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: _viewModel.loading && summary == null
          ? const Center(child: CircularProgressIndicator())
          : _viewModel.error != null && summary == null
              ? ErrorPanel(message: _viewModel.error!, onRetry: _reload)
              : _DashboardContent(user: widget.user, summary: summary!),
    );
  }
}

class _DashboardContent extends StatelessWidget {
  const _DashboardContent({required this.user, required this.summary});

  final User user;
  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      children: [
        _WelcomeCard(user: user, summary: summary),
        const SizedBox(height: 18),
        Text('Resumen del dia', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 10),
        LayoutBuilder(
          builder: (context, constraints) {
            final columns = constraints.maxWidth >= 720 ? 3 : 2;
            return GridView.count(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisCount: columns,
              mainAxisSpacing: 10,
              crossAxisSpacing: 10,
              childAspectRatio: constraints.maxWidth >= 720 ? 2.45 : 1.55,
              children: [
                _MetricCard(title: 'Ventas hoy', value: 'S/ ${summary.ventasHoy.toStringAsFixed(2)}', icon: Icons.payments_rounded),
                _MetricCard(title: 'Ventas', value: summary.cantidadVentasHoy.toString(), icon: Icons.receipt_long_rounded),
                _MetricCard(title: 'Mes', value: 'S/ ${summary.ventasMes.toStringAsFixed(2)}', icon: Icons.calendar_month_rounded),
                _MetricCard(title: 'Compras', value: 'S/ ${summary.comprasHoy.toStringAsFixed(2)}', icon: Icons.inventory_2_rounded),
                _MetricCard(title: 'Productos', value: summary.totalProductos.toString(), icon: Icons.medication_rounded),
                _MetricCard(title: 'Vencidos', value: summary.vencidos.toString(), icon: Icons.warning_amber_rounded, danger: summary.vencidos > 0),
              ],
            );
          },
        ),
        const SizedBox(height: 18),
        Text('Modulos disponibles', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
        const SizedBox(height: 10),
        _ModuleGrid(user: user),
      ],
    );
  }
}

class _WelcomeCard extends StatelessWidget {
  const _WelcomeCard({required this.user, required this.summary});

  final User user;
  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF087B68), Color(0xFF12A58D)],
        ),
        boxShadow: [
          BoxShadow(color: const Color(0xFF087B68).withValues(alpha: 0.22), blurRadius: 24, offset: const Offset(0, 12)),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Hola, ${user.nombreCompleto}', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w800)),
                const SizedBox(height: 6),
                Text('Rol: ${user.rolesLabel}', style: const TextStyle(color: Colors.white70)),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    _StatusChip(label: '${summary.totalProductos} productos'),
                    _StatusChip(label: '${summary.stockBajo} stock bajo'),
                    _StatusChip(label: '${summary.vencidos} vencidos'),
                  ],
                ),
              ],
            ),
          ),
          const SizedBox(width: 14),
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(24)),
            child: const Icon(Icons.local_pharmacy_rounded, color: Colors.white, size: 42),
          ),
        ],
      ),
    );
  }
}

class _StatusChip extends StatelessWidget {
  const _StatusChip({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(999)),
      child: Text(label, style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w600)),
    );
  }
}

class _ModuleGrid extends StatelessWidget {
  const _ModuleGrid({required this.user});

  final User user;

  @override
  Widget build(BuildContext context) {
    final modules = <_ModuleItem>[
      if (user.canViewProductos)
        const _ModuleItem('Productos', 'Inventario y busqueda', Icons.medication_rounded, Color(0xFF087B68), ProductsPage()),
      if (user.canViewVentas)
        const _ModuleItem('Ventas', 'Registro operativo', Icons.point_of_sale_rounded, Color(0xFF1C7ED6), VentasPage()),
      if (user.canViewCompras)
        const _ModuleItem('Compras', 'Abastecimiento', Icons.inventory_2_rounded, Color(0xFF6741D9), ComprasPage()),
      if (user.canViewCaja)
        const _ModuleItem('Caja', 'Control de efectivo', Icons.account_balance_wallet_rounded, Color(0xFFE67700), CajaPage()),
      if (user.canViewReportes)
        const _ModuleItem('Reportes', 'Indicadores y rangos', Icons.bar_chart_rounded, Color(0xFFC2255C), ReportesPage()),
      if (user.canViewUsuarios)
        const _ModuleItem('Usuarios', 'Acceso administrativo', Icons.group_rounded, Color(0xFF364FC7), UsuariosPage()),
    ];

    if (modules.isEmpty) {
      return const Card(child: ListTile(title: Text('No hay modulos disponibles para este rol')));
    }

    return LayoutBuilder(
      builder: (context, constraints) {
        final columns = constraints.maxWidth >= 720 ? 3 : 2;
        return GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: columns,
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            childAspectRatio: constraints.maxWidth >= 720 ? 1.65 : 1.08,
          ),
          itemCount: modules.length,
          itemBuilder: (context, index) {
            final module = modules[index];
            return Card(
              child: InkWell(
                borderRadius: BorderRadius.circular(22),
                onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => module.page)),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        width: 48,
                        height: 48,
                        decoration: BoxDecoration(color: module.color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(16)),
                        child: Icon(module.icon, color: module.color, size: 28),
                      ),
                      const Spacer(),
                      Text(module.title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w800)),
                      const SizedBox(height: 4),
                      Text(module.subtitle, style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.black54)),
                    ],
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }
}

class _ModuleItem {
  const _ModuleItem(this.title, this.subtitle, this.icon, this.color, this.page);

  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final Widget page;
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({required this.title, required this.value, required this.icon, this.danger = false});

  final String title;
  final String value;
  final IconData icon;
  final bool danger;

  @override
  Widget build(BuildContext context) {
    final color = danger ? Theme.of(context).colorScheme.error : Theme.of(context).colorScheme.primary;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Icon(icon, size: 20, color: color),
                const SizedBox(width: 8),
                Expanded(child: Text(title, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black54))),
              ],
            ),
            Text(value, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900, color: const Color(0xFF12332D))),
          ],
        ),
      ),
    );
  }
}
