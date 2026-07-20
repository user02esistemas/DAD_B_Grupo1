import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/services/operational_data_service.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../../caja/view/caja_page.dart';
import '../service/purchase_service.dart';

class ComprasPage extends StatefulWidget {
  const ComprasPage({required this.user, super.key});

  final User user;

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

  Future<void> _openNewPurchase() async {
    final saved = await Navigator.of(context).push<bool>(MaterialPageRoute(
      builder: (_) => NewPurchasePage(user: widget.user),
    ));
    if (saved == true) _load();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          leading: IconButton(
              icon: const Icon(Icons.arrow_back_rounded),
              onPressed: () => Navigator.of(context).maybePop()),
          title: const ModuleAppTitle(
              title: 'Compras', icon: Icons.inventory_2_rounded),
          actions: [
            IconButton(
                onPressed: _load, icon: const Icon(Icons.refresh_rounded))
          ],
        ),
        bottomNavigationBar: AppQuickNavBar(
          current: '',
          onHome: () =>
              Navigator.of(context).popUntil((route) => route.isFirst),
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
              title: 'Abastecimiento',
              subtitle: _loading
                  ? 'Actualizando compras'
                  : '${_data.rows.length} compras recientes',
              icon: Icons.inventory_2_rounded,
              color: brandSkyDark,
            ),
            const SizedBox(height: 12),
            if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
            ActionTile(
              title: 'Nueva compra',
              subtitle: 'Registrar ingreso de un producto sin salir del modulo',
              icon: Icons.add_shopping_cart_rounded,
              color: brandSkyDark,
              onTap: _openNewPurchase,
            ),
            ActionTile(
              title: 'Historial de compras',
              subtitle: 'Consultar ingresos recientes con paginacion',
              icon: Icons.history_rounded,
              color: brandSkyDark,
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

  void _showQuickMessage(String message) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(message),
      behavior: SnackBarBehavior.floating,
    ));
  }
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

class NewPurchasePage extends StatefulWidget {
  const NewPurchasePage({required this.user, super.key});

  final User user;

  @override
  State<NewPurchasePage> createState() => _NewPurchasePageState();
}

class _NewPurchasePageState extends State<NewPurchasePage> {
  final _formKey = GlobalKey<FormState>();
  final _searchController = TextEditingController();
  final _quantityController = TextEditingController(text: '1');
  final _purchasePriceController = TextEditingController();
  final _salePriceController = TextEditingController();
  final _lotController = TextEditingController();
  final _expirationController = TextEditingController();
  final _observationsController = TextEditingController();
  late final PurchaseService _service;
  List<Supplier> _suppliers = const [];
  List<CatalogProduct> _catalog = const [];
  Supplier? _supplier;
  CatalogProduct? _product;
  bool _loading = true;
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _service = PurchaseService(ApiClient());
    _expirationController.text = _defaultDate();
    _loadSuppliers();
  }

  @override
  void dispose() {
    _searchController.dispose();
    _quantityController.dispose();
    _purchasePriceController.dispose();
    _salePriceController.dispose();
    _lotController.dispose();
    _expirationController.dispose();
    _observationsController.dispose();
    super.dispose();
  }

  Future<void> _loadSuppliers() async {
    try {
      final suppliers = await _service.suppliers();
      if (!mounted) return;
      setState(() {
        _suppliers = suppliers;
        _supplier = suppliers.isEmpty ? null : suppliers.first;
      });
    } catch (ex) {
      if (mounted) {
        setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  Future<void> _search() async {
    final term = _searchController.text.trim();
    if (term.length < 2) return;
    setState(() => _error = null);
    try {
      final catalog = await _service.searchCatalog(term);
      if (!mounted) return;
      setState(() => _catalog = catalog);
    } catch (ex) {
      if (mounted) {
        setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
      }
    }
  }

  void _toggleProduct(CatalogProduct item) {
    setState(() {
      _product = _product?.id == item.id ? null : item;
      _error = null;
    });
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate() ||
        _supplier == null ||
        _product == null) {
      setState(
          () => _error = 'Seleccione proveedor y producto para continuar.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    final line = PurchaseLine(
      product: _product!,
      quantity: int.parse(_quantityController.text),
      purchasePrice: double.parse(_purchasePriceController.text),
      salePrice: double.parse(_salePriceController.text),
      lot: _lotController.text.trim().isEmpty
          ? 'SIN-LOTE'
          : _lotController.text.trim(),
      expirationDate: _expirationController.text.trim(),
    );
    try {
      await _service.registerPurchase(
        userId: widget.user.id,
        supplierId: _supplier!.id,
        line: line,
        observations: _observationsController.text.trim(),
      );
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Compra registrada correctamente'),
        behavior: SnackBarBehavior.floating,
      ));
      Navigator.of(context).pop(true);
    } catch (ex) {
      if (mounted) {
        setState(() => _error = ex.toString().replaceFirst('Exception: ', ''));
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  String _defaultDate() {
    final date = DateTime.now().add(const Duration(days: 365));
    return '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

  double get _total {
    final quantity = int.tryParse(_quantityController.text) ?? 0;
    final price = double.tryParse(_purchasePriceController.text) ?? 0;
    return quantity * price;
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Nueva compra')),
        body: _loading
            ? const Center(child: CircularProgressIndicator())
            : ListView(
                padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
                children: [
                  CompactModuleHeader(
                    title: 'Compra de abastecimiento',
                    subtitle: _product == null
                        ? 'Elige proveedor, busca producto y completa el ingreso'
                        : 'Producto seleccionado: ${_product!.name}',
                    icon: Icons.add_shopping_cart_rounded,
                    color: brandSkyDark,
                    trailing: Text('S/ ${_total.toStringAsFixed(2)}',
                        style: const TextStyle(fontWeight: FontWeight.w900)),
                  ),
                  const SizedBox(height: 12),
                  if (_error != null)
                    ErrorPanel(
                        message: _error!,
                        onRetry: () => setState(() => _error = null)),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Form(
                        key: _formKey,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            DropdownButtonFormField<Supplier>(
                              initialValue: _supplier,
                              isExpanded: true,
                              decoration: const InputDecoration(
                                labelText: 'Proveedor',
                                prefixIcon: Icon(Icons.local_shipping_rounded),
                              ),
                              items: _suppliers
                                  .map((supplier) => DropdownMenuItem(
                                        value: supplier,
                                        child: Text(supplier.name,
                                            overflow: TextOverflow.ellipsis),
                                      ))
                                  .toList(),
                              onChanged: (value) =>
                                  setState(() => _supplier = value),
                              validator: (value) =>
                                  value == null ? 'Seleccione proveedor' : null,
                            ),
                            const SizedBox(height: 12),
                            TextField(
                              controller: _searchController,
                              textInputAction: TextInputAction.search,
                              onSubmitted: (_) => _search(),
                              decoration: InputDecoration(
                                labelText: 'Buscar producto catalogo',
                                prefixIcon: const Icon(Icons.search_rounded),
                                suffixIcon: IconButton(
                                  onPressed: _search,
                                  icon: const Icon(Icons.arrow_forward_rounded),
                                ),
                              ),
                            ),
                            if (_product != null) ...[
                              const SizedBox(height: 12),
                              _SelectedProductCard(
                                product: _product!,
                                onRemove: () => setState(() => _product = null),
                              ),
                            ],
                            if (_catalog.isNotEmpty) ...[
                              const SizedBox(height: 14),
                              Text('Resultados del catalogo',
                                  style: Theme.of(context)
                                      .textTheme
                                      .titleSmall
                                      ?.copyWith(fontWeight: FontWeight.w900)),
                              const SizedBox(height: 6),
                            ],
                            for (final item in _catalog.take(4))
                              _CatalogResultTile(
                                product: item,
                                selected: _product?.id == item.id,
                                onTap: () => _toggleProduct(item),
                              ),
                            const Divider(height: 24),
                            Row(children: [
                              Expanded(
                                child: TextFormField(
                                  controller: _quantityController,
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(
                                      labelText: 'Cantidad'),
                                  onChanged: (_) => setState(() {}),
                                  validator: (value) {
                                    final parsed = int.tryParse(value ?? '');
                                    return parsed == null || parsed <= 0
                                        ? 'Cantidad valida'
                                        : null;
                                  },
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: TextFormField(
                                  controller: _purchasePriceController,
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(
                                      labelText: 'P. compra'),
                                  onChanged: (_) => setState(() {}),
                                  validator: (value) {
                                    final parsed = double.tryParse(value ?? '');
                                    return parsed == null || parsed <= 0
                                        ? 'Precio valido'
                                        : null;
                                  },
                                ),
                              ),
                            ]),
                            const SizedBox(height: 12),
                            Row(children: [
                              Expanded(
                                child: TextFormField(
                                  controller: _salePriceController,
                                  keyboardType: TextInputType.number,
                                  decoration: const InputDecoration(
                                      labelText: 'P. venta'),
                                  validator: (value) {
                                    final parsed = double.tryParse(value ?? '');
                                    return parsed == null || parsed <= 0
                                        ? 'Precio valido'
                                        : null;
                                  },
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: TextFormField(
                                  controller: _lotController,
                                  decoration:
                                      const InputDecoration(labelText: 'Lote'),
                                ),
                              ),
                            ]),
                            const SizedBox(height: 12),
                            TextFormField(
                              controller: _expirationController,
                              decoration: const InputDecoration(
                                labelText: 'Vencimiento (YYYY-MM-DD)',
                                prefixIcon: Icon(Icons.event_rounded),
                              ),
                              validator: (value) =>
                                  value == null || value.trim().isEmpty
                                      ? 'Ingrese vencimiento'
                                      : null,
                            ),
                            const SizedBox(height: 12),
                            TextField(
                              controller: _observationsController,
                              minLines: 2,
                              maxLines: 3,
                              decoration: const InputDecoration(
                                  labelText: 'Observaciones'),
                            ),
                            const SizedBox(height: 16),
                            FilledButton.icon(
                              onPressed: _saving ? null : _save,
                              icon: _saving
                                  ? const SizedBox(
                                      width: 18,
                                      height: 18,
                                      child: CircularProgressIndicator(
                                          strokeWidth: 2),
                                    )
                                  : const Icon(Icons.check_rounded),
                              label: const Text('Registrar compra'),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
      );
}

class _SelectedProductCard extends StatelessWidget {
  const _SelectedProductCard({required this.product, required this.onRemove});

  final CatalogProduct product;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: brandSkyDark.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(18),
          border: Border.all(color: brandSkyDark.withValues(alpha: 0.18)),
        ),
        child: Row(children: [
          const CircleAvatar(
            backgroundColor: brandSkyDark,
            foregroundColor: Colors.white,
            child: Icon(Icons.check_rounded),
          ),
          const SizedBox(width: 12),
          Expanded(
            child:
                Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              const Text('Producto seleccionado',
                  style: TextStyle(
                      color: brandSkyDark,
                      fontWeight: FontWeight.w900,
                      fontSize: 12)),
              Text(product.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(fontWeight: FontWeight.w900)),
              Text(product.presentation,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(color: Colors.black54)),
            ]),
          ),
          IconButton(
            tooltip: 'Quitar producto',
            onPressed: onRemove,
            icon: const Icon(Icons.close_rounded),
          ),
        ]),
      );
}

class _CatalogResultTile extends StatelessWidget {
  const _CatalogResultTile({
    required this.product,
    required this.selected,
    required this.onTap,
  });

  final CatalogProduct product;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => AnimatedContainer(
        duration: const Duration(milliseconds: 160),
        margin: const EdgeInsets.only(bottom: 8),
        decoration: BoxDecoration(
          color: selected ? brandSkyDark.withValues(alpha: 0.08) : Colors.white,
          borderRadius: BorderRadius.circular(18),
          border: Border.all(
            color: selected
                ? brandSkyDark.withValues(alpha: 0.35)
                : Colors.black.withValues(alpha: 0.05),
          ),
        ),
        child: ListTile(
          onTap: onTap,
          contentPadding: const EdgeInsets.symmetric(horizontal: 12),
          title: Text(product.name,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(fontWeight: FontWeight.w900)),
          subtitle: Text(product.presentation,
              maxLines: 2, overflow: TextOverflow.ellipsis),
          trailing: Icon(
            selected ? Icons.check_circle_rounded : Icons.add_circle_outline,
            color: selected ? brandSkyDark : Colors.black54,
          ),
        ),
      );
}
