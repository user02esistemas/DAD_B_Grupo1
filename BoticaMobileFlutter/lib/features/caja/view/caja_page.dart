import 'package:flutter/material.dart';

class CajaPage extends StatelessWidget {
  const CajaPage({super.key});

  @override
  Widget build(BuildContext context) {
    const color = Color(0xFFE67700);
    return Scaffold(
      appBar: AppBar(leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()), title: const Text('Caja')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          Container(
            padding: const EdgeInsets.all(18),
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(28), gradient: const LinearGradient(colors: [color, Color(0xFFF5A94A)])),
            child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Row(children: [Container(width: 54, height: 54, decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)), child: const Icon(Icons.account_balance_wallet_rounded, color: Colors.white, size: 31)), const Spacer(), const _Badge('Control diario')]),
              const SizedBox(height: 18),
              Text('Caja', style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
              const SizedBox(height: 6),
              const Text('Apertura, cierre y control de efectivo para el turno.', style: TextStyle(color: Colors.white70)),
              const SizedBox(height: 14),
              const Wrap(spacing: 8, runSpacing: 8, children: [_Badge('S/ 0.00 efectivo'), _Badge('0 tickets'), _Badge('Turno sin abrir')]),
            ]),
          ),
          const SizedBox(height: 18),
          Text('Operaciones de caja', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 10),
          const _CajaAction(icon: Icons.lock_open_rounded, title: 'Abrir caja', subtitle: 'Registrar monto inicial', color: color),
          const _CajaAction(icon: Icons.payments_rounded, title: 'Arqueo', subtitle: 'Comparar ventas y efectivo', color: Color(0xFF087B68)),
          const _CajaAction(icon: Icons.lock_rounded, title: 'Cerrar caja', subtitle: 'Consolidar el turno', color: Color(0xFFC2255C)),
          const SizedBox(height: 10),
          const Card(child: Padding(padding: EdgeInsets.all(16), child: Text('El backend actual aun no expone endpoints de sesion de caja. La pantalla queda preparada con el flujo real del sistema, sin pasos tecnicos ni contenido provisional.'))),
        ],
      ),
    );
  }
}

class _CajaAction extends StatelessWidget {
  const _CajaAction({required this.icon, required this.title, required this.subtitle, required this.color});
  final IconData icon;
  final String title;
  final String subtitle;
  final Color color;
  @override
  Widget build(BuildContext context) => Card(child: ListTile(leading: CircleAvatar(backgroundColor: color.withValues(alpha: 0.12), child: Icon(icon, color: color)), title: Text(title, style: const TextStyle(fontWeight: FontWeight.w900)), subtitle: Text(subtitle), trailing: const Icon(Icons.chevron_right_rounded)));
}

class _Badge extends StatelessWidget {
  const _Badge(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)));
}
