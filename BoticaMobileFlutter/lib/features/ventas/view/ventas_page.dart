import 'dart:async';

import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../../caja/view/caja_page.dart';
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
        title: const ModuleAppTitle(
            title: 'Ventas', icon: Icons.point_of_sale_rounded),
        actions: [
          IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))
        ],
      ),
      bottomNavigationBar: AppQuickNavBar(
        current: '',
        onHome: () => Navigator.of(context).popUntil((route) => route.isFirst),
        onAlerts: () => _showQuickMessage('Revisa las alertas desde Inicio.'),
        onCaja: widget.user.canViewCaja
            ? () => Navigator.of(context).push(MaterialPageRoute(
                  builder: (_) => CajaPage(user: widget.user),
                ))
            : () => _showQuickMessage('No tienes acceso a Caja.'),
        onProfile: () => showQuickProfileSheet(
          context,
          name: widget.user.nombreCompleto,
          username: widget.user.username,
          role: widget.user.rolesLabel,
        ),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 104),
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
          _NewSaleActionButton(
            enabled: cashSession != null,
            onTap: _openNewSale,
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

  void _showQuickMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(message),
      behavior: SnackBarBehavior.floating,
    ));
  }
}

class NewSalePage extends StatefulWidget {
  const NewSalePage({required this.user, super.key});
  final User user;

  @override
  State<NewSalePage> createState() => _NewSalePageState();
}

class _NewSaleActionButton extends StatelessWidget {
  const _NewSaleActionButton({required this.enabled, required this.onTap});

  final bool enabled;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final subtitle = enabled
        ? 'Buscar por nombre, agregar al carrito y cobrar'
        : 'Primero abre caja para vender';
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: enabled ? onTap : null,
          borderRadius: BorderRadius.circular(28),
          child: Ink(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(28),
              gradient: enabled
                  ? const LinearGradient(
                      colors: [Color(0xFF0369A1), Color(0xFF22C7D9)],
                    )
                  : null,
              color: enabled ? null : moduleSurface(context),
              border: Border.all(
                color: enabled
                    ? Colors.transparent
                    : brandSky.withValues(alpha: 0.14),
              ),
              boxShadow: enabled
                  ? [
                      BoxShadow(
                        color: brandSkyDark.withValues(alpha: 0.22),
                        blurRadius: 24,
                        offset: const Offset(0, 12),
                      )
                    ]
                  : null,
            ),
            child: Row(children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  color: enabled
                      ? Colors.white.withValues(alpha: 0.18)
                      : brandSky.withValues(alpha: 0.10),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Icon(Icons.add_shopping_cart_rounded,
                    color: enabled ? Colors.white : brandSkyDark, size: 30),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('Nueva venta',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              color: enabled
                                  ? Colors.white
                                  : moduleTextPrimary(context),
                              fontWeight: FontWeight.w900,
                            )),
                    const SizedBox(height: 2),
                    Text(subtitle,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          color: enabled
                              ? Colors.white.withValues(alpha: 0.82)
                              : moduleTextSecondary(context),
                        )),
                  ],
                ),
              ),
              const SizedBox(width: 10),
              Container(
                width: 42,
                height: 42,
                decoration: BoxDecoration(
                  color: enabled
                      ? Colors.white.withValues(alpha: 0.20)
                      : brandSky.withValues(alpha: 0.10),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(Icons.arrow_forward_rounded,
                    color: enabled ? Colors.white : moduleTextMuted(context)),
              ),
            ]),
          ),
        ),
      ),
    );
  }
}

class _NewSalePageState extends State<NewSalePage> {
  final _customer = TextEditingController(text: 'CLIENTES VARIOS');
  final _search = TextEditingController();
  late final ProductService _productService;
  late final SalesService _salesService;
  List<Product> _products = const [];
  final List<SaleItem> _items = [];
  Timer? _searchDebounce;
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
    _searchDebounce?.cancel();
    _customer.dispose();
    _search.dispose();
    super.dispose();
  }

  double get _total => _items.fold<double>(0, (sum, item) => sum + item.total);
  int get _quantity => _items.fold(0, (sum, item) => sum + item.quantity);

  Future<void> _loadProducts({bool keepKeyboardOpen = false}) async {
    if (!keepKeyboardOpen) FocusManager.instance.primaryFocus?.unfocus();
    final term = _search.text.trim();
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final products = await _productService.search(
        term: term,
        limit: term.isEmpty ? 2 : 8,
      );
      if (!mounted) return;
      setState(() => _products = products);
    } catch (ex) {
      if (!mounted) return;
      setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _onSearchChanged(String value) {
    _searchDebounce?.cancel();
    _searchDebounce = Timer(const Duration(milliseconds: 320), () async {
      if (!mounted) return;
      if (value.trim().isEmpty || value.trim().length >= 2) {
        await _loadProducts(keepKeyboardOpen: true);
      }
    });
  }

  void _add(Product product) {
    FocusManager.instance.primaryFocus?.unfocus();
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

  void _removeItem(int index) {
    setState(() => _items.removeAt(index));
  }

  Future<void> _save() async {
    if (_items.isEmpty) return;
    FocusManager.instance.primaryFocus?.unfocus();
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
        appBar: AppBar(
            title: const ModuleAppTitle(
                title: 'Nueva venta', icon: Icons.point_of_sale_rounded)),
        body: _NewSaleBody(
          customer: _customer,
          search: _search,
          products: _products,
          items: _items,
          loading: _loading,
          error: _error,
          paymentMethod: _paymentMethod,
          total: _total,
          quantity: _quantity,
          saving: _saving,
          onSearch: _loadProducts,
          onSearchChanged: _onSearchChanged,
          onAdd: _add,
          onQuantity: _changeQuantity,
          onRemove: _removeItem,
          onPayment: (value) {
            FocusManager.instance.primaryFocus?.unfocus();
            setState(() => _paymentMethod = value);
          },
          onSave: _save,
        ),
      );
}

class _NewSaleBody extends StatelessWidget {
  const _NewSaleBody({
    required this.customer,
    required this.search,
    required this.products,
    required this.items,
    required this.loading,
    required this.error,
    required this.paymentMethod,
    required this.total,
    required this.quantity,
    required this.saving,
    required this.onSearch,
    required this.onSearchChanged,
    required this.onAdd,
    required this.onQuantity,
    required this.onRemove,
    required this.onPayment,
    required this.onSave,
  });

  final TextEditingController customer;
  final TextEditingController search;
  final List<Product> products;
  final List<SaleItem> items;
  final bool loading;
  final String? error;
  final String paymentMethod;
  final double total;
  final int quantity;
  final bool saving;
  final VoidCallback onSearch;
  final ValueChanged<String> onSearchChanged;
  final ValueChanged<Product> onAdd;
  final void Function(int index, int delta) onQuantity;
  final ValueChanged<int> onRemove;
  final ValueChanged<String> onPayment;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) {
    final query = search.text.trim();
    final visibleProducts = products.take(query.isEmpty ? 2 : 5).toList();
    return SafeArea(
      child: ListView(
        keyboardDismissBehavior: ScrollViewKeyboardDismissBehavior.onDrag,
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
        children: [
          _PosHeader(total: total, quantity: quantity, method: paymentMethod),
          const SizedBox(height: 12),
          TextField(
            controller: customer,
            decoration: const InputDecoration(
                prefixIcon: Icon(Icons.person_rounded), labelText: 'Cliente'),
          ),
          const SizedBox(height: 10),
          TextField(
            controller: search,
            textInputAction: TextInputAction.search,
            onSubmitted: (_) => onSearch(),
            onChanged: onSearchChanged,
            decoration: InputDecoration(
              prefixIcon: const Icon(Icons.search_rounded),
              hintText: 'Buscar por nombre',
              helperText: query.isEmpty
                  ? 'Escribe al menos 2 letras para buscar.'
                  : 'Mostrando coincidencias por nombre.',
              suffixIcon: IconButton(
                  onPressed: onSearch,
                  icon: const Icon(Icons.arrow_forward_rounded)),
            ),
          ),
          if (error != null) ErrorPanel(message: error!, onRetry: onSearch),
          const SizedBox(height: 12),
          _PosSectionTitle(
            title: query.isEmpty ? 'Productos rapidos' : 'Resultados',
            label: query.isEmpty
                ? '${visibleProducts.length} rapidos'
                : '${visibleProducts.length} encontrados',
            icon: Icons.medication_rounded,
          ),
          if (loading)
            const Card(
                child: Padding(
                    padding: EdgeInsets.all(16),
                    child: Text('Cargando productos...'))),
          if (!loading && visibleProducts.isEmpty)
            const Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Text('No hay coincidencias con ese nombre.'),
              ),
            ),
          for (final product in visibleProducts)
            _PosProductTile(
              product: product,
              inCart: items
                  .where((item) => item.product.id == product.id)
                  .fold(0, (sum, item) => sum + item.quantity),
              onAdd: () => onAdd(product),
            ),
          const SizedBox(height: 12),
          _PosSectionTitle(
            title: 'Carrito de venta',
            label: '$quantity items',
            icon: Icons.shopping_cart_checkout_rounded,
          ),
          if (items.isEmpty) const _EmptyCartCard(),
          for (var i = 0; i < items.length; i++)
            _CartLineTile(
              item: items[i],
              onMinus: () => onQuantity(i, -1),
              onPlus: () => onQuantity(i, 1),
              onRemove: () => onRemove(i),
            ),
          const SizedBox(height: 8),
          _PaymentMethods(selected: paymentMethod, onSelected: onPayment),
          const SizedBox(height: 14),
          _CheckoutPanel(
            total: total,
            saving: saving,
            enabled: items.isNotEmpty,
            onSave: onSave,
          ),
        ],
      ),
    );
  }
}

class _PosHeader extends StatelessWidget {
  const _PosHeader(
      {required this.total, required this.quantity, required this.method});

  final double total;
  final int quantity;
  final String method;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(28),
          gradient: const LinearGradient(
            colors: [Color(0xFF0369A1), Color(0xFF38BDF8)],
          ),
          boxShadow: [
            BoxShadow(
              color: brandSkyDark.withValues(alpha: 0.18),
              blurRadius: 24,
              offset: const Offset(0, 12),
            )
          ],
        ),
        child: Row(children: [
          Container(
            width: 58,
            height: 58,
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.18),
              borderRadius: BorderRadius.circular(20),
            ),
            child: const Icon(Icons.point_of_sale_rounded,
                color: Colors.white, size: 32),
          ),
          const SizedBox(width: 14),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Text('Punto de venta',
                  style: TextStyle(
                      color: Colors.white70, fontWeight: FontWeight.w800)),
              Text('S/ ${total.toStringAsFixed(2)}',
                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                      color: Colors.white, fontWeight: FontWeight.w900)),
              Text('$quantity productos - ${_methodLabel(method)}',
                  style: const TextStyle(color: Colors.white70)),
            ]),
          ),
        ]),
      );
}

class _PosSectionTitle extends StatelessWidget {
  const _PosSectionTitle(
      {required this.title, required this.label, required this.icon});

  final String title;
  final String label;
  final IconData icon;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Row(children: [
          Icon(icon, color: brandSkyDark, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Text(title,
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.w900)),
          ),
          Chip(
            label: Text(label),
            backgroundColor: brandSky.withValues(alpha: 0.12),
            side: BorderSide.none,
          ),
        ]),
      );
}

class _PosProductTile extends StatelessWidget {
  const _PosProductTile(
      {required this.product, required this.inCart, required this.onAdd});

  final Product product;
  final int inCart;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) => Card(
        child: InkWell(
          onTap: onAdd,
          borderRadius: BorderRadius.circular(22),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Row(children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: brandSky.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(18),
                ),
                child: const Icon(Icons.add_shopping_cart_rounded,
                    color: brandSkyDark),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(product.nombre,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                              color: moduleTextPrimary(context),
                              fontWeight: FontWeight.w900)),
                      Text(
                          'Stock ${product.stockActual}${inCart > 0 ? ' - en carrito $inCart' : ''}',
                          style:
                              TextStyle(color: moduleTextSecondary(context))),
                    ]),
              ),
              const SizedBox(width: 8),
              Column(crossAxisAlignment: CrossAxisAlignment.end, children: [
                Text('S/ ${product.precioVenta.toStringAsFixed(2)}',
                    style: const TextStyle(
                        color: brandSkyDark, fontWeight: FontWeight.w900)),
                const SizedBox(height: 6),
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                  decoration: BoxDecoration(
                    color: brandSkyDark,
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: const Text('Agregar',
                      style: TextStyle(
                          color: Colors.white,
                          fontSize: 11,
                          fontWeight: FontWeight.w900)),
                ),
              ]),
            ]),
          ),
        ),
      );
}

class _EmptyCartCard extends StatelessWidget {
  const _EmptyCartCard();

  @override
  Widget build(BuildContext context) => Card(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(children: [
            CircleAvatar(
              backgroundColor: brandSky.withValues(alpha: 0.12),
              child:
                  const Icon(Icons.shopping_cart_outlined, color: brandSkyDark),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text('Agregue productos para iniciar el cobro.',
                  style: TextStyle(color: moduleTextSecondary(context))),
            ),
          ]),
        ),
      );
}

class _CartLineTile extends StatelessWidget {
  const _CartLineTile(
      {required this.item,
      required this.onMinus,
      required this.onPlus,
      required this.onRemove});

  final SaleItem item;
  final VoidCallback onMinus;
  final VoidCallback onPlus;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context) => Card(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              Expanded(
                child: Text(item.product.nombre,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        color: moduleTextPrimary(context),
                        fontWeight: FontWeight.w900)),
              ),
              IconButton(
                tooltip: 'Eliminar producto',
                onPressed: onRemove,
                icon: const Icon(Icons.delete_outline_rounded),
                color: Colors.redAccent,
              ),
            ]),
            const SizedBox(height: 4),
            Text(
                'S/ ${item.product.precioVenta.toStringAsFixed(2)} x ${item.quantity}',
                style: TextStyle(color: moduleTextSecondary(context))),
            const SizedBox(height: 10),
            Row(children: [
              Text('Subtotal S/ ${item.total.toStringAsFixed(2)}',
                  style: const TextStyle(fontWeight: FontWeight.w900)),
              const Spacer(),
              _QuantityButton(icon: Icons.remove_rounded, onTap: onMinus),
              Container(
                width: 42,
                alignment: Alignment.center,
                child: Text(item.quantity.toString(),
                    style: const TextStyle(
                        fontSize: 18, fontWeight: FontWeight.w900)),
              ),
              _QuantityButton(
                  icon: Icons.add_rounded, onTap: onPlus, filled: true),
            ]),
          ]),
        ),
      );
}

class _QuantityButton extends StatelessWidget {
  const _QuantityButton({
    required this.icon,
    required this.onTap,
    this.filled = false,
  });

  final IconData icon;
  final VoidCallback onTap;
  final bool filled;

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: filled ? brandSkyDark : brandSky.withValues(alpha: 0.16),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Icon(icon, color: filled ? Colors.white : brandSkyDark),
        ),
      );
}

class _CheckoutPanel extends StatelessWidget {
  const _CheckoutPanel({
    required this.total,
    required this.saving,
    required this.enabled,
    required this.onSave,
  });

  final double total;
  final bool saving;
  final bool enabled;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: moduleSurface(context),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: brandSky.withValues(alpha: 0.10)),
          boxShadow: [
            BoxShadow(
              color: brandDark.withValues(
                  alpha: isDarkMode(context) ? 0.28 : 0.10),
              blurRadius: 22,
              offset: const Offset(0, 10),
            )
          ],
        ),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Total a cobrar',
              style: TextStyle(
                  color: moduleTextSecondary(context),
                  fontWeight: FontWeight.w700)),
          const SizedBox(height: 2),
          Text('S/ ${total.toStringAsFixed(2)}',
              style: Theme.of(context)
                  .textTheme
                  .headlineSmall
                  ?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 12),
          SizedBox(
            width: double.infinity,
            height: 52,
            child: FilledButton.icon(
              onPressed: !enabled || saving ? null : onSave,
              icon: saving
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.payments_rounded),
              label: const Text('Cobrar venta'),
            ),
          ),
        ]),
      );
}

class _PaymentMethods extends StatelessWidget {
  const _PaymentMethods({required this.selected, required this.onSelected});

  final String selected;
  final ValueChanged<String> onSelected;

  @override
  Widget build(BuildContext context) => Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Metodo de pago',
              style: Theme.of(context)
                  .textTheme
                  .titleMedium
                  ?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 8),
          _PaymentCard(
            value: 'EFECTIVO',
            selected: selected,
            icon: Icons.payments_rounded,
            title: 'Efectivo',
            subtitle: 'Cobro en caja',
            onSelected: onSelected,
          ),
          _PaymentCard(
            value: 'YAPE_PLIN',
            selected: selected,
            icon: Icons.qr_code_2_rounded,
            title: 'Yape / Plin',
            subtitle: 'Pago movil por QR',
            onSelected: onSelected,
          ),
          _PaymentCard(
            value: 'TARJETA',
            selected: selected,
            icon: Icons.credit_card_rounded,
            title: 'Tarjeta',
            subtitle: 'POS debito o credito',
            onSelected: onSelected,
          ),
        ],
      );
}

class _PaymentCard extends StatelessWidget {
  const _PaymentCard({
    required this.value,
    required this.selected,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onSelected,
  });

  final String value;
  final String selected;
  final IconData icon;
  final String title;
  final String subtitle;
  final ValueChanged<String> onSelected;

  @override
  Widget build(BuildContext context) {
    final active = selected == value;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        onTap: () => onSelected(value),
        borderRadius: BorderRadius.circular(22),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 180),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: active
                ? brandSkyDark.withValues(
                    alpha: isDarkMode(context) ? 0.28 : 0.10)
                : moduleSurface(context),
            borderRadius: BorderRadius.circular(22),
            border: Border.all(
              color: active ? brandSkyDark : brandSky.withValues(alpha: 0.12),
              width: active ? 1.6 : 1,
            ),
          ),
          child: Row(children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: active ? brandSkyDark : brandSky.withValues(alpha: 0.14),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: active ? Colors.white : brandSkyDark),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(title,
                        style: TextStyle(
                            color: moduleTextPrimary(context),
                            fontWeight: FontWeight.w900)),
                    Text(subtitle,
                        style: TextStyle(
                            color: moduleTextSecondary(context), fontSize: 12)),
                  ]),
            ),
            Icon(
              active ? Icons.check_circle_rounded : Icons.circle_outlined,
              color: active ? brandSkyDark : moduleTextMuted(context),
            ),
          ]),
        ),
      ),
    );
  }
}

String _methodLabel(String value) => switch (value) {
      'YAPE_PLIN' => 'Yape/Plin',
      'TARJETA' => 'Tarjeta',
      _ => 'Efectivo',
    };

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
