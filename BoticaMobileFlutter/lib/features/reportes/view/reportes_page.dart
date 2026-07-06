import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../caja/service/cash_service.dart';

class ReportesPage extends StatefulWidget {
  const ReportesPage({super.key});

  @override
  State<ReportesPage> createState() => _ReportesPageState();
}

class _ReportesPageState extends State<ReportesPage> {
  late final OperationalDataService _service;
  late final CashService _cashService;
  SalesReportData? _report;
  ModuleData _sales = const ModuleData(rows: [], stats: []);
  ModuleData _purchases = const ModuleData(rows: [], stats: []);
  List<CashSession> _sessions = const [];
  String? _error;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    final apiClient = ApiClient();
    _service = OperationalDataService(apiClient);
    _cashService = CashService(apiClient);
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    final now = DateTime.now();
    final today = _formatDate(DateTime(now.year, now.month, now.day));
    try {
      final report = await _service.salesReport(desde: today, hasta: today);
      final sales = await _service.latestSales(limit: 2);
      final purchases = await _service.purchases(limit: 2);
      final sessions = await _cashService.recentSessions(limit: 2);
      if (!mounted) return;
      setState(() {
        _report = report;
        _sales = sales;
        _purchases = purchases;
        _sessions = sessions;
      });
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final report = _report;
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            icon: const Icon(Icons.arrow_back_rounded),
            onPressed: () => Navigator.of(context).maybePop()),
        title: const Text('Reportes'),
        actions: [
          IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          CompactModuleHeader(
            title: report == null
                ? 'Resumen operativo'
                : 'S/ ${report.totalGeneral}',
            subtitle: report == null
                ? 'Ventas, compras y caja'
                : 'Ventas de hoy - ${report.cantidadVentas} tickets',
            icon: Icons.bar_chart_rounded,
          ),
          const SizedBox(height: 12),
          if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
          if (_loading)
            const Card(
                child: Padding(
                    padding: EdgeInsets.all(16),
                    child: Text('Cargando reportes...'))),
          if (report != null) ...[
            _PaymentSummary(report: report),
            const SizedBox(height: 10),
            _ReportMixCard(report: report),
          ],
          _ActivityTabs(
              sales: _sales.rows,
              purchases: _purchases.rows,
              sessions: _sessions),
        ],
      ),
    );
  }
}

class _ReportMixCard extends StatelessWidget {
  const _ReportMixCard({required this.report});
  final SalesReportData report;

  @override
  Widget build(BuildContext context) {
    final values = [
      _ReportSlice('Efectivo', _money(report.totalEfectivo), brandRed),
      _ReportSlice(
          'Yape/Plin', _money(report.totalYapePlin), const Color(0xFF7C3AED)),
      _ReportSlice(
          'Tarjeta', _money(report.totalTarjeta), const Color(0xFF1F5F8B)),
      _ReportSlice('Mixto', _money(report.totalMixto), const Color(0xFF8A5A16)),
    ];
    final total = values.fold<double>(0, (sum, item) => sum + item.value);
    final top = values.where((item) => item.value > 0).fold<_ReportSlice?>(null,
        (best, item) => best == null || item.value > best.value ? item : best);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(children: [
          SizedBox(
            width: 104,
            height: 104,
            child: CustomPaint(
              painter: _ReportDonutPainter(values),
              child: Center(
                child: Text('${report.cantidadVentas}\ntickets',
                    textAlign: TextAlign.center,
                    style: const TextStyle(fontWeight: FontWeight.w900)),
              ),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Text('Mezcla de pagos',
                  style: TextStyle(fontWeight: FontWeight.w900, fontSize: 16)),
              const SizedBox(height: 6),
              Text(
                total == 0
                    ? 'Sin ventas registradas hoy'
                    : 'Predomina ${top?.label ?? 'venta'} con S/ ${(top?.value ?? 0).toStringAsFixed(2)}',
                style: const TextStyle(color: Colors.black54),
              ),
              const SizedBox(height: 8),
              Text('Promedio S/ ${report.promedioVenta}',
                  style: const TextStyle(fontWeight: FontWeight.w800)),
            ]),
          ),
        ]),
      ),
    );
  }
}

class _PaymentSummary extends StatelessWidget {
  const _PaymentSummary({required this.report});
  final SalesReportData report;

  @override
  Widget build(BuildContext context) => SizedBox(
        height: 70,
        child: ListView(
          scrollDirection: Axis.horizontal,
          children: [
            _PaymentPill(
              icon: Icons.payments_rounded,
              label: 'Efectivo',
              value: 'S/ ${report.totalEfectivo}',
              color: brandRed,
            ),
            _PaymentPill(
              icon: Icons.qr_code_2_rounded,
              label: 'Yape/Plin',
              value: 'S/ ${report.totalYapePlin}',
              color: const Color(0xFF7C3AED),
            ),
            _PaymentPill(
              icon: Icons.credit_card_rounded,
              label: 'Tarjeta',
              value: 'S/ ${report.totalTarjeta}',
              color: const Color(0xFF1F5F8B),
            ),
          ],
        ),
      );
}

class _PaymentPill extends StatelessWidget {
  const _PaymentPill(
      {required this.icon,
      required this.label,
      required this.value,
      required this.color});
  final IconData icon;
  final String label;
  final String value;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
        width: 150,
        margin: const EdgeInsets.only(right: 10),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(22),
          border: Border.all(color: color.withValues(alpha: 0.18)),
        ),
        child: Row(children: [
          CircleAvatar(
            radius: 18,
            backgroundColor: color.withValues(alpha: 0.12),
            child: Icon(icon, color: color, size: 19),
          ),
          const SizedBox(width: 9),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black54, fontSize: 12)),
              Text(value,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(color: color, fontWeight: FontWeight.w900)),
            ]),
          ),
        ]),
      );
}

class _ActivityTabs extends StatefulWidget {
  const _ActivityTabs(
      {required this.sales, required this.purchases, required this.sessions});
  final List<ModuleRow> sales;
  final List<ModuleRow> purchases;
  final List<CashSession> sessions;

  @override
  State<_ActivityTabs> createState() => _ActivityTabsState();
}

class _ActivityTabsState extends State<_ActivityTabs> {
  String _tab = 'ventas';

  @override
  Widget build(BuildContext context) {
    return Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
      const SizedBox(height: 14),
      Text('Actividad reciente',
          style: Theme.of(context)
              .textTheme
              .titleLarge
              ?.copyWith(fontWeight: FontWeight.w900)),
      const SizedBox(height: 6),
      SizedBox(
        height: 40,
        child: ListView(scrollDirection: Axis.horizontal, children: [
          _TabChip(
              label: 'Ventas', value: 'ventas', selected: _tab, onTap: _setTab),
          _TabChip(
              label: 'Compras',
              value: 'compras',
              selected: _tab,
              onTap: _setTab),
          _TabChip(
              label: 'Caja', value: 'caja', selected: _tab, onTap: _setTab),
        ]),
      ),
      const SizedBox(height: 8),
      if (_tab == 'ventas')
        _RowsView(
            rows: widget.sales,
            icon: Icons.receipt_long_rounded,
            empty: 'No hay ventas recientes.'),
      if (_tab == 'compras')
        _RowsView(
            rows: widget.purchases,
            icon: Icons.inventory_2_rounded,
            empty: 'No hay compras recientes.'),
      if (_tab == 'caja') _CashRowsView(sessions: widget.sessions),
    ]);
  }

  void _setTab(String value) => setState(() => _tab = value);
}

class _TabChip extends StatelessWidget {
  const _TabChip(
      {required this.label,
      required this.value,
      required this.selected,
      required this.onTap});
  final String label;
  final String value;
  final String selected;
  final ValueChanged<String> onTap;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(right: 6),
        child: ChoiceChip(
          label: Text(label),
          selected: selected == value,
          onSelected: (_) => onTap(value),
          visualDensity: VisualDensity.compact,
        ),
      );
}

class _RowsView extends StatelessWidget {
  const _RowsView(
      {required this.rows, required this.icon, required this.empty});
  final List<ModuleRow> rows;
  final IconData icon;
  final String empty;

  @override
  Widget build(BuildContext context) => Column(children: [
        for (final row in rows.take(2))
          CompactRecordCard(
              title: row.title,
              subtitle: row.subtitle,
              footnote: row.footnote,
              value: row.value,
              icon: icon),
        if (rows.isEmpty)
          Card(
              child: Padding(
                  padding: const EdgeInsets.all(16), child: Text(empty))),
      ]);
}

class _CashRowsView extends StatelessWidget {
  const _CashRowsView({required this.sessions});
  final List<CashSession> sessions;

  @override
  Widget build(BuildContext context) => Column(children: [
        for (final session in sessions)
          CompactRecordCard(
            title: session.cajaNombre,
            subtitle: 'Inicial S/ ${session.montoInicial.toStringAsFixed(2)}',
            footnote:
                'Esperado S/ ${session.efectivoEsperado.toStringAsFixed(2)}',
            value: 'Caja ${session.id}',
            icon: Icons.account_balance_wallet_rounded,
          ),
        if (sessions.isEmpty)
          const Card(
              child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Text('No hay sesiones de caja.'))),
      ]);
}

class _ReportSlice {
  const _ReportSlice(this.label, this.value, this.color);
  final String label;
  final double value;
  final Color color;
}

class _ReportDonutPainter extends CustomPainter {
  const _ReportDonutPainter(this.values);
  final List<_ReportSlice> values;

  @override
  void paint(Canvas canvas, Size size) {
    final stroke = size.width * 0.13;
    final rect = Offset.zero & size;
    final total = values.fold<double>(0, (sum, item) => sum + item.value);
    final base = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = stroke
      ..strokeCap = StrokeCap.round
      ..color = brandRed.withValues(alpha: 0.08);
    canvas.drawArc(rect.deflate(stroke / 2), -1.57, 6.28, false, base);
    if (total <= 0) return;
    var start = -1.57;
    for (final item in values.where((item) => item.value > 0)) {
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
  bool shouldRepaint(covariant _ReportDonutPainter oldDelegate) =>
      oldDelegate.values != values;
}

double _money(String value) {
  final cleaned = value.replaceAll('S/', '').replaceAll(',', '').trim();
  return double.tryParse(cleaned) ?? 0;
}

String _formatDate(DateTime date) {
  final month = date.month.toString().padLeft(2, '0');
  final day = date.day.toString().padLeft(2, '0');
  return '${date.year}-$month-$day';
}
