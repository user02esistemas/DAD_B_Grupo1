import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../../core/widgets/error_panel.dart';
import '../auth/user.dart';
import '../productos/products_page.dart';
import 'dashboard_service.dart';
import 'dashboard_summary.dart';

class HomePage extends StatefulWidget {
  const HomePage({required this.user, super.key});

  final User user;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  late final DashboardService _dashboardService;
  late Future<DashboardSummary> _summaryFuture;

  @override
  void initState() {
    super.initState();
    _dashboardService = DashboardService(ApiClient());
    _summaryFuture = _dashboardService.getSummary();
  }

  void _reload() {
    setState(() => _summaryFuture = _dashboardService.getSummary());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        actions: [
          IconButton(onPressed: _reload, icon: const Icon(Icons.refresh)),
        ],
      ),
      body: FutureBuilder<DashboardSummary>(
        future: _summaryFuture,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return ErrorPanel(message: snapshot.error.toString(), onRetry: _reload);
          }
          final summary = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text('Hola, ${widget.user.nombreCompleto}', style: Theme.of(context).textTheme.titleLarge),
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
            ],
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.of(context).push(MaterialPageRoute(builder: (_) => const ProductsPage()));
        },
        icon: const Icon(Icons.search),
        label: const Text('Productos'),
      ),
    );
  }
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
