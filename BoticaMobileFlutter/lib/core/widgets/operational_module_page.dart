import 'package:flutter/material.dart';

import '../network/api_client.dart';
import '../services/operational_data_service.dart';
import 'error_panel.dart';

class OperationalModulePage extends StatefulWidget {
  const OperationalModulePage({required this.title, required this.description, required this.icon, required this.color, required this.loader, required this.primaryAction, super.key});

  final String title;
  final String description;
  final IconData icon;
  final Color color;
  final Future<ModuleData> Function(OperationalDataService service) loader;
  final String primaryAction;

  @override
  State<OperationalModulePage> createState() => _OperationalModulePageState();
}

class _OperationalModulePageState extends State<OperationalModulePage> {
  late final OperationalDataService _service;
  late Future<ModuleData> _future;

  @override
  void initState() {
    super.initState();
    _service = OperationalDataService(ApiClient());
    _future = widget.loader(_service);
  }

  void _reload() => setState(() => _future = widget.loader(_service));

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()), title: Text(widget.title), actions: [IconButton(onPressed: _reload, icon: const Icon(Icons.refresh_rounded))]),
      body: FutureBuilder<ModuleData>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) return const Center(child: CircularProgressIndicator());
          if (snapshot.hasError) return ErrorPanel(message: snapshot.error.toString(), onRetry: _reload);
          final data = snapshot.data ?? const ModuleData(rows: [], stats: []);
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
            children: [
              _Hero(widget: widget, data: data),
              const SizedBox(height: 18),
              Text(widget.primaryAction, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
              const SizedBox(height: 10),
              if (data.rows.isEmpty) const Card(child: Padding(padding: EdgeInsets.all(18), child: Text('No hay registros para mostrar.'))),
              for (final row in data.rows) _RecordCard(row: row, color: widget.color),
            ],
          );
        },
      ),
    );
  }
}

class _Hero extends StatelessWidget {
  const _Hero({required this.widget, required this.data});
  final OperationalModulePage widget;
  final ModuleData data;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(28), gradient: LinearGradient(colors: [widget.color, Color.lerp(widget.color, Colors.white, 0.28)!])),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [Container(width: 54, height: 54, decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)), child: Icon(widget.icon, color: Colors.white, size: 31)), const Spacer(), const _Badge('Conectado al sistema')]),
        const SizedBox(height: 18),
        Text(widget.title, style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
        const SizedBox(height: 6),
        Text(widget.description, style: const TextStyle(color: Colors.white70)),
        const SizedBox(height: 14),
        Wrap(spacing: 8, runSpacing: 8, children: data.stats.map((stat) => _Badge('${stat.value} ${stat.label}')).toList()),
      ]),
    );
  }
}

class _RecordCard extends StatelessWidget {
  const _RecordCard({required this.row, required this.color});
  final ModuleRow row;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: CircleAvatar(backgroundColor: color.withValues(alpha: 0.12), child: Icon(Icons.receipt_long_rounded, color: color)),
        title: Text(row.title, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w900)),
        subtitle: Text('${row.subtitle}\n${row.footnote}', maxLines: 2, overflow: TextOverflow.ellipsis),
        trailing: Text(row.value, style: TextStyle(color: color, fontWeight: FontWeight.w900)),
      ),
    );
  }
}

class _Badge extends StatelessWidget {
  const _Badge(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)));
}
