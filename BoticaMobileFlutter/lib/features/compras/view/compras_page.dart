import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';

class ComprasPage extends StatefulWidget {
  const ComprasPage({super.key});

  @override
  State<ComprasPage> createState() => _ComprasPageState();
}

class _ComprasPageState extends State<ComprasPage> {
  late final OperationalDataService _service;
  ModuleData _data = const ModuleData(rows: [], stats: []);
  String? _error;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _service = OperationalDataService(ApiClient());
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final data = await _service.purchases();
      if (!mounted) return;
      setState(() => _data = data);
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _openHistory() {
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => PurchasesHistoryPage(rows: _data.rows),
    ));
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          leading: IconButton(
              icon: const Icon(Icons.arrow_back_rounded),
              onPressed: () => Navigator.of(context).maybePop()),
          title: const Text('Compras'),
          actions: [
            IconButton(
                onPressed: _load, icon: const Icon(Icons.refresh_rounded))
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
          children: [
            CompactModuleHeader(
              title: 'Abastecimiento',
              subtitle: _loading
                  ? 'Actualizando compras'
                  : '${_data.rows.length} compras recientes',
              icon: Icons.inventory_2_rounded,
              color: brandRed,
            ),
            const SizedBox(height: 12),
            if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
            ActionTile(
              title: 'Historial de compras',
              subtitle: 'Consultar ingresos recientes con paginacion',
              icon: Icons.history_rounded,
              onTap: _data.rows.isEmpty ? null : _openHistory,
            ),
            const SizedBox(height: 12),
            Text('Vista rapida',
                style: Theme.of(context)
                    .textTheme
                    .titleLarge
                    ?.copyWith(fontWeight: FontWeight.w900)),
            const SizedBox(height: 8),
            if (_loading)
              const Card(
                  child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Text('Cargando compras...'))),
            for (final row in _data.rows.take(2))
              CompactRecordCard(
                title: row.title,
                subtitle: row.subtitle,
                footnote: row.footnote,
                value: row.value,
                icon: Icons.inventory_2_rounded,
              ),
          ],
        ),
      );
}

class PurchasesHistoryPage extends StatefulWidget {
  const PurchasesHistoryPage({required this.rows, super.key});
  final List<ModuleRow> rows;

  @override
  State<PurchasesHistoryPage> createState() => _PurchasesHistoryPageState();
}

class _PurchasesHistoryPageState extends State<PurchasesHistoryPage> {
  static const _pageSize = 5;
  int _page = 0;

  @override
  Widget build(BuildContext context) {
    final totalPages = (widget.rows.length / _pageSize).ceil().clamp(1, 999);
    final rows = widget.rows.skip(_page * _pageSize).take(_pageSize);
    return Scaffold(
      appBar: AppBar(title: const Text('Historial de compras')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          for (final row in rows)
            CompactRecordCard(
              title: row.title,
              subtitle: row.subtitle,
              footnote: row.footnote,
              value: row.value,
              icon: Icons.inventory_2_rounded,
            ),
          PagerBar(
            page: _page + 1,
            totalPages: totalPages,
            onPrevious: _page == 0 ? null : () => setState(() => _page--),
            onNext:
                _page >= totalPages - 1 ? null : () => setState(() => _page++),
          ),
        ],
      ),
    );
  }
}
