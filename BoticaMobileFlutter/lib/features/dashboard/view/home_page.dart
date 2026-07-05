import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
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
    if (mounted) setState(() {});
  }

  void _reload() => _viewModel.load();

  @override
  Widget build(BuildContext context) {
    final summary = _viewModel.summary;
    return Scaffold(
      drawer: _AppDrawer(user: widget.user),
      appBar: AppBar(
        title: const Text('Panel movil'),
        actions: [IconButton(onPressed: _reload, icon: const Icon(Icons.refresh_rounded))],
      ),
      body: _viewModel.loading && summary == null
          ? const Center(child: CircularProgressIndicator())
          : _viewModel.error != null && summary == null
              ? ErrorPanel(message: _viewModel.error!, onRetry: _reload)
              : _DashboardContent(user: widget.user, summary: summary!, onReload: _reload),
    );
  }
}

class _DashboardContent extends StatelessWidget {
  const _DashboardContent({required this.user, required this.summary, required this.onReload});

  final User user;
  final DashboardSummary summary;
  final VoidCallback onReload;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
      children: [
        _SearchBar(onReload: onReload),
        const SizedBox(height: 14),
        _HeroPanel(user: user, summary: summary),
        const SizedBox(height: 18),
        const _SectionTitle(title: 'Estadisticas', label: 'Hoy'),
        const SizedBox(height: 10),
        _MetricsGrid(summary: summary),
        const SizedBox(height: 20),
        _RiskCard(summary: summary),
        const SizedBox(height: 22),
        _SectionTitle(title: 'Modulos operativos', label: '${_modulesFor(user).length} activos'),
        const SizedBox(height: 10),
        _ModuleGrid(user: user),
      ],
    );
  }
}

class _SearchBar extends StatelessWidget {
  const _SearchBar({required this.onReload});

  final VoidCallback onReload;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Container(
            height: 50,
            padding: const EdgeInsets.symmetric(horizontal: 14),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(18)),
            child: const Row(
              children: [
                Icon(Icons.search_rounded, color: Colors.black38),
                SizedBox(width: 10),
                Expanded(child: Text('Buscar producto o modulo', style: TextStyle(color: Colors.black38))),
              ],
            ),
          ),
        ),
        const SizedBox(width: 10),
        _SquareButton(icon: Icons.notifications_none_rounded, onTap: () {}),
        const SizedBox(width: 8),
        _SquareButton(icon: Icons.sync_rounded, onTap: onReload),
      ],
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
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(30),
        gradient: const LinearGradient(colors: [Color(0xFF087B68), Color(0xFF15B89C)]),
        boxShadow: [BoxShadow(color: const Color(0xFF087B68).withValues(alpha: 0.22), blurRadius: 24, offset: const Offset(0, 12))],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 54,
                height: 54,
                decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)),
                child: const Icon(Icons.local_pharmacy_rounded, color: Colors.white, size: 32),
              ),
              const Spacer(),
              const _HeroBadge(label: 'API + RMI activo'),
            ],
          ),
          const SizedBox(height: 20),
          Text('Hola, ${user.nombreCompleto}', style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
          const SizedBox(height: 6),
          Text('Rol: ${user.rolesLabel}', style: const TextStyle(color: Colors.white70)),
          const SizedBox(height: 16),
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _HeroBadge(label: 'S/ ${summary.ventasHoy.toStringAsFixed(2)} hoy'),
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
      _MetricData('Ventas hoy', 'S/ ${summary.ventasHoy.toStringAsFixed(2)}', Icons.payments_rounded, const Color(0xFF087B68), 'operativo'),
      _MetricData('Tickets', summary.cantidadVentasHoy.toString(), Icons.receipt_long_rounded, const Color(0xFF1C7ED6), 'emitidos'),
      _MetricData('Ventas mes', 'S/ ${summary.ventasMes.toStringAsFixed(2)}', Icons.show_chart_rounded, const Color(0xFF6741D9), 'acumulado'),
      _MetricData('Compras', 'S/ ${summary.comprasHoy.toStringAsFixed(2)}', Icons.inventory_2_rounded, const Color(0xFFE67700), 'hoy'),
      _MetricData('Productos', summary.totalProductos.toString(), Icons.medication_rounded, const Color(0xFF0CA678), 'activos'),
      _MetricData('Vencidos', summary.vencidos.toString(), Icons.warning_amber_rounded, const Color(0xFFE03131), 'revision'),
    ];

    return LayoutBuilder(
      builder: (context, constraints) {
        final columns = constraints.maxWidth >= 760 ? 3 : 2;
        return GridView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: data.length,
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: columns,
            mainAxisSpacing: 10,
            crossAxisSpacing: 10,
            mainAxisExtent: 134,
          ),
          itemBuilder: (_, index) => _MetricCard(data: data[index]),
        );
      },
    );
  }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({required this.data});

  final _MetricData data;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(13),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(color: data.color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(13)),
                  child: Icon(data.icon, color: data.color, size: 21),
                ),
                const Spacer(),
                Icon(Icons.more_horiz_rounded, color: Colors.black.withValues(alpha: 0.25)),
              ],
            ),
            const Spacer(),
            Text(data.value, maxLines: 1, overflow: TextOverflow.ellipsis, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900, color: const Color(0xFF12332D))),
            Text(data.title, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 13)),
            Text(data.hint, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black45, fontSize: 11)),
          ],
        ),
      ),
    );
  }
}

class _RiskCard extends StatelessWidget {
  const _RiskCard({required this.summary});

  final DashboardSummary summary;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Control de riesgo', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
            const SizedBox(height: 12),
            _RiskRow(label: 'Stock bajo', value: summary.stockBajo, color: const Color(0xFFE67700)),
            _RiskRow(label: 'Agotados', value: summary.agotados, color: const Color(0xFFE03131)),
            _RiskRow(label: 'Por vencer', value: summary.porVencer, color: const Color(0xFF1C7ED6)),
            _RiskRow(label: 'Vencidos', value: summary.vencidos, color: const Color(0xFFC2255C)),
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
            mainAxisSpacing: 12,
            crossAxisSpacing: 12,
            mainAxisExtent: 208,
          ),
          itemBuilder: (_, index) => _ModuleCard(module: modules[index]),
        );
      },
    );
  }
}

class _ModuleCard extends StatelessWidget {
  const _ModuleCard({required this.module});

  final _ModuleItem module;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        borderRadius: BorderRadius.circular(22),
        onTap: () => _openModule(context, module.page),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Container(
                    width: 46,
                    height: 46,
                    decoration: BoxDecoration(color: module.color.withValues(alpha: 0.14), borderRadius: BorderRadius.circular(16)),
                    child: Icon(module.icon, color: module.color, size: 27),
                  ),
                  const Spacer(),
                  Icon(Icons.arrow_forward_rounded, color: module.color),
                ],
              ),
              const SizedBox(height: 14),
              Text(module.title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
              const SizedBox(height: 4),
              Text(module.subtitle, maxLines: 2, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black54, fontSize: 12)),
              const Spacer(),
              Wrap(spacing: 5, runSpacing: 5, children: module.actions.map((e) => _ActionPill(label: e, color: module.color)).toList()),
            ],
          ),
        ),
      ),
    );
  }
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
              Text('EconoSalud', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
              const SizedBox(height: 6),
              Text(user.rolesLabel, style: const TextStyle(color: Colors.black54)),
              const SizedBox(height: 18),
              _DrawerItem(icon: Icons.dashboard_rounded, label: 'Dashboard', onTap: () => Navigator.of(context).maybePop()),
              for (final module in _modulesFor(user)) _DrawerItem(icon: module.icon, label: module.title, onTap: () => _openModule(context, module.page)),
              const Spacer(),
              _DrawerItem(icon: Icons.logout_rounded, label: 'Salir', onTap: () => _logout(context)),
            ],
          ),
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
      messenger.showSnackBar(const SnackBar(content: Text('No se pudo cerrar sesion en el servidor. Se cerrara localmente.')));
    }
    navigator.pushAndRemoveUntil(MaterialPageRoute(builder: (_) => const LoginPage()), (route) => false);
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title, required this.label});

  final String title;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      Text(title, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
      const Spacer(),
      Chip(label: Text(label), backgroundColor: Colors.white, side: BorderSide.none),
    ]);
  }
}

class _HeroBadge extends StatelessWidget {
  const _HeroBadge({required this.label});
  final String label;
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
        decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(999)),
        child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w700, fontSize: 12)),
      );
}

class _ActionPill extends StatelessWidget {
  const _ActionPill({required this.label, required this.color});
  final String label;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(color: color.withValues(alpha: 0.10), borderRadius: BorderRadius.circular(999)),
        child: Text(label, style: TextStyle(color: color, fontSize: 10, fontWeight: FontWeight.w800)),
      );
}

class _RiskRow extends StatelessWidget {
  const _RiskRow({required this.label, required this.value, required this.color});
  final String label;
  final int value;
  final Color color;
  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(children: [
          Container(width: 10, height: 10, decoration: BoxDecoration(color: color, shape: BoxShape.circle)),
          const SizedBox(width: 9),
          Expanded(child: Text(label)),
          Text(value.toString(), style: const TextStyle(fontWeight: FontWeight.w900)),
        ]),
      );
}

class _SquareButton extends StatelessWidget {
  const _SquareButton({required this.icon, required this.onTap});
  final IconData icon;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => Material(
        color: Colors.white,
        borderRadius: BorderRadius.circular(18),
        child: InkWell(borderRadius: BorderRadius.circular(18), onTap: onTap, child: SizedBox(width: 50, height: 50, child: Icon(icon))),
      );
}

class _DrawerItem extends StatelessWidget {
  const _DrawerItem({required this.icon, required this.label, required this.onTap});
  final IconData icon;
  final String label;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => ListTile(leading: Icon(icon), title: Text(label), onTap: onTap, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)));
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
  const _ModuleItem(this.title, this.subtitle, this.icon, this.color, this.page, this.actions);
  final String title;
  final String subtitle;
  final IconData icon;
  final Color color;
  final Widget page;
  final List<String> actions;
}

List<_ModuleItem> _modulesFor(User user) => [
      if (user.canViewProductos) const _ModuleItem('Productos', 'Inventario, stock y vencimientos', Icons.medication_rounded, Color(0xFF087B68), ProductsPage(), ['Buscar', 'Stock']),
      if (user.canViewVentas) const _ModuleItem('Ventas', 'Atencion, comprobante y ultimas ventas', Icons.point_of_sale_rounded, Color(0xFF1C7ED6), VentasPage(), ['Carrito', 'Cobrar']),
      if (user.canViewCompras) const _ModuleItem('Compras', 'Proveedores e ingresos', Icons.inventory_2_rounded, Color(0xFF6741D9), ComprasPage(), ['Proveedor', 'Ingreso']),
      if (user.canViewCaja) const _ModuleItem('Caja', 'Apertura, cierre y control diario', Icons.account_balance_wallet_rounded, Color(0xFFE67700), CajaPage(), ['Abrir', 'Cerrar']),
      if (user.canViewReportes) const _ModuleItem('Reportes', 'Ventas por rango y productos top', Icons.bar_chart_rounded, Color(0xFFC2255C), ReportesPage(), ['Rangos', 'Top']),
      if (user.canViewUsuarios) const _ModuleItem('Usuarios', 'Roles, perfiles y accesos', Icons.group_rounded, Color(0xFF364FC7), UsuariosPage(), ['Roles', 'Acceso']),
    ];

Future<void> _openModule(BuildContext context, Widget page) => Navigator.of(context).push(MaterialPageRoute(builder: (_) => page));
