import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../../caja/service/cash_service.dart';
import '../../productos/model/product.dart';
import '../../productos/service/product_service.dart';
import '../service/sales_service.dart';

class VentasPage extends StatefulWidget {
  const VentasPage({required this.user, super.key});

  final User user;

  @override
  State<VentasPage> createState() => _VentasPageState();
}

class _VentasPageState extends State<VentasPage> {
  late final OperationalDataService _dataService;
  late final CashService _cashService;
  ModuleData _salesData = const ModuleData(rows: [], stats: []);
  CashSession? _cashSession;
  String? _error;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    final apiClient = ApiClient();
    _dataService = OperationalDataService(apiClient);
    _cashService = CashService(apiClient);
    _load();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final sales = await _dataService.latestSales(limit: 20);
      final cashSession = await _cashService.activeSession(widget.user.id);
      if (!mounted) return;
      setState(() {
        _salesData = sales;
        _cashSession = cashSession;
      });
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _openNewSale() async {
    await Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => NewSalePage(user: widget.user),
    ));
    _load();
  }

  void _openHistory() {
    Navigator.of(context).push(MaterialPageRoute(
      builder: (_) => SalesHistoryPage(rows: _salesData.rows),
    ));
  }

  @override
  Widget build(BuildContext context) {
    final cashSession = _cashSession;
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            icon: const Icon(Icons.arrow_back_rounded),
            onPressed: () => Navigator.of(context).maybePop()),
        title: const Text('Ventas'),
        actions: [
          IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          CompactModuleHeader(
            title: 'Ventas del turno',
            subtitle: cashSession == null
                ? 'Abra caja para vender'
                : 'Caja abierta - efectivo S/ ${cashSession.efectivoEsperado.toStringAsFixed(2)}',
            icon: Icons.point_of_sale_rounded,
            color: brandRed,
            trailing: _loading
                ? const SizedBox(
                    width: 22,
                    height: 22,
                    child: CircularProgressIndicator(strokeWidth: 2))
                : null,
          ),
          const SizedBox(height: 12),
          if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
          if (cashSession == null)
            const Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Text('Debe abrir caja antes de registrar ventas.'),
              ),
            ),
          ActionTile(
            title: 'Nueva venta',
            subtitle: cashSession == null
                ? 'Disponible al abrir caja'
                : 'Buscar productos y armar carrito',
            icon: Icons.add_shopping_cart_rounded,
            onTap: cashSession == null ? null : _openNewSale,
          ),
          ActionTile(
            title: 'Historial de ventas',
            subtitle: 'Consultar ventas recientes con paginacion',
            icon: Icons.history_rounded,
            color: const Color(0xFF8A5A16),
            onTap: _salesData.rows.isEmpty ? null : _openHistory,
          ),
          const SizedBox(height: 12),
          Text('Vista rapida',
              style: Theme.of(context)
                  .textTheme
                  .titleLarge
                  ?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 8),
          for (final row in _salesData.rows.take(2))
            CompactRecordCard(
              title: row.title,
              subtitle: row.subtitle,
              footnote: row.footnote,
              value: row.value,
              color: brandRed,
            ),
          if (!_loading && _salesData.rows.isEmpty)
            const Card(
                child: Padding(
                    padding: EdgeInsets.all(16),
                    child: Text('No hay ventas recientes.'))),
        ],
      ),
    );
  }
}

class NewSalePage extends StatefulWidget {
  const NewSalePage({required this.user, super.key});
  final User user;

  @override
  State<NewSalePage> createState() => _NewSalePageState();
}

class _NewSalePageState extends State<NewSalePage> {
  final _customer = TextEditingController(text: 'CLIENTES VARIOS');
  final _search = TextEditingController();
  late final ProductService _productService;
  late final SalesService _salesService;
  List<Product> _products = const [];
  final List<SaleItem> _items = [];
  String _paymentMethod = 'EFECTIVO';
  bool _loading = true;
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    final apiClient = ApiClient();
    _productService = ProductService(apiClient);
    _salesService = SalesService(apiClient);
    _loadProducts();
  }

  @override
  void dispose() {
    _customer.dispose();
    _search.dispose();
    super.dispose();
  }

  double get _total => _items.fold(0, (sum, item) => sum + item.total);

  Future<void> _loadProducts() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final products =
          await _productService.search(term: _search.text.trim(), limit: 20);
      if (!mounted) return;
      setState(() => _products = products);
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _add(Product product) {
    final index = _items.indexWhere((item) => item.product.id == product.id);
    setState(() {
      if (index == -1) {
        _items.add(SaleItem(product: product, quantity: 1));
      } else if (_items[index].quantity < product.stockActual) {
        _items[index] =
            _items[index].copyWith(quantity: _items[index].quantity + 1);
      }
    });
  }

  void _changeQuantity(int index, int delta) {
    final item = _items[index];
    final next = item.quantity + delta;
    setState(() {
      if (next <= 0) {
        _items.removeAt(index);
      } else if (next <= item.product.stockActual) {
        _items[index] = item.copyWith(quantity: next);
      }
    });
  }

  Future<void> _save() async {
    if (_items.isEmpty) return;
    setState(() => _saving = true);
    try {
      await _salesService.registerCartSale(
        userId: widget.user.id,
        items: _items,
        paymentMethod: _paymentMethod,
        customerName: _customer.text,
      );
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text('Venta registrada')));
      Navigator.of(context).pop();
    } catch (ex) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(ex.toString().replaceFirst('Exception: ', ''))));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Nueva venta')),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 110),
          children: [
            TextField(
              controller: _customer,
              decoration: const InputDecoration(
                  prefixIcon: Icon(Icons.person_rounded), labelText: 'Cliente'),
            ),
            const SizedBox(height: 10),
            TextField(
              controller: _search,
              textInputAction: TextInputAction.search,
              onSubmitted: (_) => _loadProducts(),
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search_rounded),
                hintText: 'Buscar producto',
                suffixIcon: IconButton(
                    onPressed: _loadProducts,
                    icon: const Icon(Icons.arrow_forward_rounded)),
              ),
            ),
            if (_error != null)
              ErrorPanel(message: _error!, onRetry: _loadProducts),
            const SizedBox(height: 12),
            Text('Productos',
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.w900)),
            if (_loading)
              const Card(
                  child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Text('Cargando productos...'))),
            for (final product in _products.take(6))
              CompactRecordCard(
                title: product.nombre,
                subtitle: 'Stock ${product.stockActual}',
                value: 'S/ ${product.precioVenta.toStringAsFixed(2)}',
                icon: Icons.add_rounded,
                onTap: () => _add(product),
              ),
            const SizedBox(height: 12),
            Text('Carrito',
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.w900)),
            if (_items.isEmpty)
              const Card(
                  child: Padding(
                      padding: EdgeInsets.all(16),
                      child: Text('Agregue productos para continuar.'))),
            for (var i = 0; i < _items.length; i++)
              Card(
                child: ListTile(
                  title: Text(_items[i].product.nombre,
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  subtitle: Text(
                      'S/ ${_items[i].product.precioVenta.toStringAsFixed(2)} x ${_items[i].quantity}'),
                  trailing: Row(mainAxisSize: MainAxisSize.min, children: [
                    IconButton(
                        onPressed: () => _changeQuantity(i, -1),
                        icon: const Icon(Icons.remove_rounded)),
                    Text(_items[i].quantity.toString()),
                    IconButton(
                        onPressed: () => _changeQuantity(i, 1),
                        icon: const Icon(Icons.add_rounded)),
                  ]),
                ),
              ),
            const SizedBox(height: 8),
            Wrap(spacing: 8, children: [
              _PayChip('EFECTIVO', _paymentMethod,
                  (v) => setState(() => _paymentMethod = v)),
              _PayChip('YAPE_PLIN', _paymentMethod,
                  (v) => setState(() => _paymentMethod = v)),
              _PayChip('TARJETA', _paymentMethod,
                  (v) => setState(() => _paymentMethod = v)),
            ]),
          ],
        ),
        bottomNavigationBar: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: FilledButton.icon(
              onPressed: _items.isEmpty || _saving ? null : _save,
              icon: _saving
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.check_rounded),
              label: Text('Cobrar S/ ${_total.toStringAsFixed(2)}'),
            ),
          ),
        ),
      );
}

class SalesHistoryPage extends StatefulWidget {
  const SalesHistoryPage({required this.rows, super.key});
  final List<ModuleRow> rows;

  @override
  State<SalesHistoryPage> createState() => _SalesHistoryPageState();
}

class _SalesHistoryPageState extends State<SalesHistoryPage> {
  static const _pageSize = 5;
  int _page = 0;

  @override
  Widget build(BuildContext context) {
    final totalPages = (widget.rows.length / _pageSize).ceil().clamp(1, 999);
    final rows = widget.rows.skip(_page * _pageSize).take(_pageSize);
    return Scaffold(
      appBar: AppBar(title: const Text('Historial de ventas')),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          for (final row in rows)
            CompactRecordCard(
                title: row.title,
                subtitle: row.subtitle,
                footnote: row.footnote,
                value: row.value),
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

class _PayChip extends StatelessWidget {
  const _PayChip(this.value, this.selected, this.onSelected);
  final String value;
  final String selected;
  final ValueChanged<String> onSelected;

  @override
  Widget build(BuildContext context) => ChoiceChip(
        label: Text(value.replaceAll('_', '/')),
        selected: selected == value,
        onSelected: (_) => onSelected(value),
      );
}
