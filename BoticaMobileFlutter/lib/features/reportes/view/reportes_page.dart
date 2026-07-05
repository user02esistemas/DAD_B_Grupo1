import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';

class ReportesPage extends StatefulWidget {
  const ReportesPage({super.key});

  @override
  State<ReportesPage> createState() => _ReportesPageState();
}

class _ReportesPageState extends State<ReportesPage> {
  late final OperationalDataService _service;
  late DateTime _desde;
  late DateTime _hasta;
  late Future<SalesReportData> _future;

  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    _desde = DateTime(now.year, now.month);
    _hasta = DateTime(now.year, now.month, now.day);
    _service = OperationalDataService(ApiClient());
    _future = _load();
  }

  Future<SalesReportData> _load() => _service.salesReport(desde: _formatDate(_desde), hasta: _formatDate(_hasta));
  void _reload() => setState(() => _future = _load());

  void _setRange(_ReportRange range) {
    final now = DateTime.now();
    setState(() {
      switch (range) {
        case _ReportRange.today:
          _desde = DateTime(now.year, now.month, now.day);
          _hasta = _desde;
        case _ReportRange.week:
          _desde = DateTime(now.year, now.month, now.day).subtract(Duration(days: now.weekday - 1));
          _hasta = DateTime(now.year, now.month, now.day);
        case _ReportRange.month:
          _desde = DateTime(now.year, now.month);
          _hasta = DateTime(now.year, now.month, now.day);
      }
      _future = _load();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()), title: const Text('Reportes'), actions: [IconButton(onPressed: _reload, icon: const Icon(Icons.refresh_rounded))]),
      body: FutureBuilder<SalesReportData>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) return const Center(child: CircularProgressIndicator());
          if (snapshot.hasError) return ErrorPanel(message: snapshot.error.toString(), onRetry: _reload);
          final report = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
            children: [
              _ReportHero(report: report),
              const SizedBox(height: 14),
              _RangeSelector(onSelected: _setRange, label: '${report.desde} al ${report.hasta}'),
              const SizedBox(height: 18),
              const _SectionTitle(title: 'Metodo de pago'),
              const SizedBox(height: 10),
              _PaymentGrid(report: report),
              const SizedBox(height: 18),
              const _SectionTitle(title: 'Ventas por dia'),
              const SizedBox(height: 10),
              if (report.ventasPorDia.isEmpty) const _EmptyCard('No hay ventas por dia en este rango.') else for (final row in report.ventasPorDia.take(7)) _ReportRow(row: row, color: const Color(0xFFC2255C)),
              const SizedBox(height: 18),
              const _SectionTitle(title: 'Detalle de ventas'),
              const SizedBox(height: 10),
              if (report.ventas.isEmpty) const _EmptyCard('No hay ventas registradas en este rango.') else for (final row in report.ventas.take(10)) _ReportRow(row: row, color: const Color(0xFF087B68)),
            ],
          );
        },
      ),
    );
  }
}

class _ReportHero extends StatelessWidget {
  const _ReportHero({required this.report});
  final SalesReportData report;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(28), gradient: const LinearGradient(colors: [Color(0xFFC2255C), Color(0xFFE36A96)])),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [Container(width: 54, height: 54, decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)), child: const Icon(Icons.bar_chart_rounded, color: Colors.white, size: 31)), const Spacer(), const _HeroBadge('Reporte de ventas')]),
        const SizedBox(height: 18),
        Text('S/ ${report.totalGeneral}', style: Theme.of(context).textTheme.displaySmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
        const SizedBox(height: 4),
        const Text('Total vendido en el rango seleccionado', style: TextStyle(color: Colors.white70)),
        const SizedBox(height: 14),
        Wrap(spacing: 8, runSpacing: 8, children: [_HeroBadge('${report.cantidadVentas} ventas'), _HeroBadge('Promedio S/ ${report.promedioVenta}'), _HeroBadge('${report.ventasPorDia.length} dias')]),
      ]),
    );
  }
}

class _RangeSelector extends StatelessWidget {
  const _RangeSelector({required this.onSelected, required this.label});
  final ValueChanged<_ReportRange> onSelected;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Filtro rapido', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 4),
          Text(label, style: const TextStyle(color: Colors.black54)),
          const SizedBox(height: 12),
          Wrap(spacing: 8, runSpacing: 8, children: [
            _RangeChip(label: 'Hoy', onTap: () => onSelected(_ReportRange.today)),
            _RangeChip(label: 'Semana', onTap: () => onSelected(_ReportRange.week)),
            _RangeChip(label: 'Mes', onTap: () => onSelected(_ReportRange.month)),
          ]),
        ]),
      ),
    );
  }
}

class _PaymentGrid extends StatelessWidget {
  const _PaymentGrid({required this.report});
  final SalesReportData report;

  @override
  Widget build(BuildContext context) {
    final items = [
      _PayItem('Efectivo', report.totalEfectivo, Icons.payments_rounded, const Color(0xFF087B68)),
      _PayItem('Yape/Plin', report.totalYapePlin, Icons.phone_iphone_rounded, const Color(0xFF6741D9)),
      _PayItem('Tarjeta', report.totalTarjeta, Icons.credit_card_rounded, const Color(0xFF1C7ED6)),
      _PayItem('Mixto', report.totalMixto, Icons.call_split_rounded, const Color(0xFFE67700)),
    ];
    return LayoutBuilder(builder: (context, constraints) {
      final columns = constraints.maxWidth >= 760 ? 4 : 2;
      return GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        itemCount: items.length,
        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(crossAxisCount: columns, mainAxisSpacing: 10, crossAxisSpacing: 10, mainAxisExtent: 116),
        itemBuilder: (_, index) => _PaymentCard(item: items[index]),
      );
    });
  }
}

class _PaymentCard extends StatelessWidget {
  const _PaymentCard({required this.item});
  final _PayItem item;
  @override
  Widget build(BuildContext context) => Card(child: Padding(padding: const EdgeInsets.all(13), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Container(width: 36, height: 36, decoration: BoxDecoration(color: item.color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(13)), child: Icon(item.icon, color: item.color, size: 20)), const Spacer(), Text('S/ ${item.value}', maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w900)), Text(item.label, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black54, fontSize: 12))])));
}

class _ReportRow extends StatelessWidget {
  const _ReportRow({required this.row, required this.color});
  final ModuleRow row;
  final Color color;
  @override
  Widget build(BuildContext context) => Card(child: ListTile(leading: CircleAvatar(backgroundColor: color.withValues(alpha: 0.12), child: Icon(Icons.receipt_long_rounded, color: color)), title: Text(row.title, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w900)), subtitle: Text('${row.subtitle}\n${row.footnote}', maxLines: 2, overflow: TextOverflow.ellipsis), trailing: Text(row.value, style: TextStyle(color: color, fontWeight: FontWeight.w900))));
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title});
  final String title;
  @override
  Widget build(BuildContext context) => Text(title, style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900));
}

class _RangeChip extends StatelessWidget {
  const _RangeChip({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => ActionChip(label: Text(label), onPressed: onTap, backgroundColor: Colors.white, side: BorderSide.none, avatar: Icon(Icons.calendar_month_rounded, size: 17, color: Theme.of(context).colorScheme.primary));
}

class _HeroBadge extends StatelessWidget {
  const _HeroBadge(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)));
}

class _EmptyCard extends StatelessWidget {
  const _EmptyCard(this.text);
  final String text;
  @override
  Widget build(BuildContext context) => Card(child: Padding(padding: const EdgeInsets.all(16), child: Text(text)));
}

class _PayItem {
  const _PayItem(this.label, this.value, this.icon, this.color);
  final String label;
  final String value;
  final IconData icon;
  final Color color;
}

enum _ReportRange { today, week, month }

String _formatDate(DateTime date) {
  final month = date.month.toString().padLeft(2, '0');
  final day = date.day.toString().padLeft(2, '0');
  return '${date.year}-$month-$day';
}
