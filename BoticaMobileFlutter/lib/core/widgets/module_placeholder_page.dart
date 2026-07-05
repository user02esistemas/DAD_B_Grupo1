import 'package:flutter/material.dart';

class ModulePlaceholderPage extends StatelessWidget {
  const ModulePlaceholderPage({required this.title, required this.description, required this.nextSteps, super.key});

  final String title;
  final String description;
  final List<String> nextSteps;

  @override
  Widget build(BuildContext context) {
    final color = _moduleColor(title);
    return Scaffold(
      appBar: AppBar(leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()), title: Text(title)),
      floatingActionButton: FloatingActionButton.extended(onPressed: () {}, icon: const Icon(Icons.add_rounded), label: const Text('Nueva accion')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(18, 8, 18, 92),
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(28), gradient: LinearGradient(colors: [color, Color.lerp(color, Colors.white, 0.28)!])),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(children: [Container(width: 58, height: 58, decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)), child: Icon(_moduleIcon(title), color: Colors.white, size: 34)), const Spacer(), const _StatusBadge(label: 'En construccion')]),
              const SizedBox(height: 22),
              Text(title, style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900, color: Colors.white)),
              const SizedBox(height: 8),
              Text(description, style: const TextStyle(color: Colors.white70)),
              const SizedBox(height: 16),
              const Wrap(spacing: 8, runSpacing: 8, children: [_HeroButton(label: 'Resumen', icon: Icons.dashboard_customize_rounded), _HeroButton(label: 'Registrar', icon: Icons.add_circle_outline_rounded), _HeroButton(label: 'Historial', icon: Icons.history_rounded)]),
            ]),
          ),
          const SizedBox(height: 20),
          Text('Acciones del modulo', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 10),
          LayoutBuilder(builder: (context, constraints) {
            final columns = constraints.maxWidth >= 700 ? 4 : 2;
            return GridView.count(shrinkWrap: true, physics: const NeverScrollableScrollPhysics(), crossAxisCount: columns, mainAxisSpacing: 10, crossAxisSpacing: 10, mainAxisExtent: 136, children: [_ActionCard(title: 'Registrar', subtitle: 'Crear operacion', icon: Icons.edit_document, color: color), const _ActionCard(title: 'Consultar', subtitle: 'Buscar historial', icon: Icons.manage_search_rounded, color: Color(0xFF1C7ED6)), const _ActionCard(title: 'Alertas', subtitle: 'Validaciones', icon: Icons.notifications_active_rounded, color: Color(0xFFE67700)), const _ActionCard(title: 'Reporte', subtitle: 'Resumen rapido', icon: Icons.bar_chart_rounded, color: Color(0xFF6741D9))]);
          }),
          const SizedBox(height: 20),
          Text('Siguientes pasos tecnicos', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 10),
          for (var index = 0; index < nextSteps.length; index++) _TimelineStep(number: index + 1, text: nextSteps[index], color: color),
          const SizedBox(height: 12),
          OutlinedButton.icon(onPressed: () => Navigator.of(context).maybePop(), icon: const Icon(Icons.arrow_back_rounded), label: const Text('Volver al dashboard')),
        ],
      ),
    );
  }

  IconData _moduleIcon(String title) => switch (title) { 'Ventas' => Icons.point_of_sale_rounded, 'Compras' => Icons.inventory_2_rounded, 'Caja' => Icons.account_balance_wallet_rounded, 'Reportes' => Icons.bar_chart_rounded, 'Usuarios' => Icons.group_rounded, _ => Icons.apps_rounded };
  Color _moduleColor(String title) => switch (title) { 'Ventas' => const Color(0xFF1C7ED6), 'Compras' => const Color(0xFF6741D9), 'Caja' => const Color(0xFFE67700), 'Reportes' => const Color(0xFFC2255C), 'Usuarios' => const Color(0xFF364FC7), _ => const Color(0xFF087B68) };
}

class _StatusBadge extends StatelessWidget { const _StatusBadge({required this.label}); final String label; @override Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800))); }
class _HeroButton extends StatelessWidget { const _HeroButton({required this.label, required this.icon}); final String label; final IconData icon; @override Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Row(mainAxisSize: MainAxisSize.min, children: [Icon(icon, size: 16, color: Colors.white), const SizedBox(width: 6), Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800))])); }
class _ActionCard extends StatelessWidget { const _ActionCard({required this.title, required this.subtitle, required this.icon, required this.color}); final String title; final String subtitle; final IconData icon; final Color color; @override Widget build(BuildContext context) => Card(child: Padding(padding: const EdgeInsets.all(14), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Container(width: 42, height: 42, decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(14)), child: Icon(icon, color: color)), const Spacer(), Text(title, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)), Text(subtitle, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black45, fontSize: 12))]))); }
class _TimelineStep extends StatelessWidget { const _TimelineStep({required this.number, required this.text, required this.color}); final int number; final String text; final Color color; @override Widget build(BuildContext context) => Padding(padding: const EdgeInsets.only(bottom: 8), child: Card(child: ListTile(leading: CircleAvatar(backgroundColor: color.withValues(alpha: 0.12), child: Text('$number', style: TextStyle(color: color, fontWeight: FontWeight.w900))), title: Text(text), trailing: Icon(Icons.chevron_right_rounded, color: color)))); }
