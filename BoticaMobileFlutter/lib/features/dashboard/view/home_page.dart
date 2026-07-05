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
        title: const Text('Dashboard'),
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
      padding: const EdgeInsets.all(16),
      children: [
        Text('Hola, ${user.nombreCompleto}', style: Theme.of(context).textTheme.titleLarge),
        const SizedBox(height: 4),
        Text('Roles: ${user.rolesLabel}', style: Theme.of(context).textTheme.bodyMedium),
        const SizedBox(height: 16),
        _MetricCard(title: 'Ventas hoy', value: 'S/ ${summary.ventasHoy.toStringAsFixed(2)}'),
        _MetricCard(title: 'Ventas registradas hoy', value: summary.cantidadVentasHoy.toString()),
        _MetricCard(title: 'Ventas del mes', value: 'S/ ${summary.ventasMes.toStringAsFixed(2)}'),
        _MetricCard(title: 'Compras hoy', value: 'S/ ${summary.comprasHoy.toStringAsFixed(2)}'),
        _MetricCard(title: 'Productos activos', value: summary.totalProductos.toString()),
        _MetricCard(title: 'Stock bajo', value: summary.stockBajo.toString()),
        _MetricCard(title: 'Agotados', value: summary.agotados.toString()),
        _MetricCard(title: 'Por vencer', value: summary.porVencer.toString()),
        _MetricCard(title: 'Vencidos', value: summary.vencidos.toString()),
        const SizedBox(height: 16),
        Text('Modulos disponibles', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        _ModuleGrid(user: user),
      ],
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
        const _ModuleItem('Productos', Icons.medication_rounded, ProductsPage()),
      if (user.canViewVentas)
        const _ModuleItem('Ventas', Icons.point_of_sale_rounded, VentasPage()),
      if (user.canViewCompras)
        const _ModuleItem('Compras', Icons.inventory_2_rounded, ComprasPage()),
      if (user.canViewCaja)
        const _ModuleItem('Caja', Icons.account_balance_wallet_rounded, CajaPage()),
      if (user.canViewReportes)
        const _ModuleItem('Reportes', Icons.bar_chart_rounded, ReportesPage()),
      if (user.canViewUsuarios)
        const _ModuleItem('Usuarios', Icons.group_rounded, UsuariosPage()),
    ];

    if (modules.isEmpty) {
      return const Card(child: ListTile(title: Text('No hay modulos disponibles para este rol')));
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        mainAxisSpacing: 10,
        crossAxisSpacing: 10,
        childAspectRatio: 1.35,
      ),
      itemCount: modules.length,
      itemBuilder: (context, index) {
        final module = modules[index];
        return Card(
          child: InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: () => Navigator.of(context).push(MaterialPageRoute(builder: (_) => module.page)),
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(module.icon, size: 34, color: Theme.of(context).colorScheme.primary),
                  const SizedBox(height: 8),
                  Text(module.title, textAlign: TextAlign.center, style: const TextStyle(fontWeight: FontWeight.bold)),
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}

class _ModuleItem {
  const _ModuleItem(this.title, this.icon, this.page);

  final String title;
  final IconData icon;
  final Widget page;
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({required this.title, required this.value});

  final String title;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: Text(title),
        trailing: Text(value, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
      ),
    );
  }
}
