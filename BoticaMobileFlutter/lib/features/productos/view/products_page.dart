import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../../auth/model/user.dart';
import '../../caja/view/caja_page.dart';
import '../model/product.dart';
import '../service/product_service.dart';

class ProductsPage extends StatefulWidget {
  const ProductsPage({required this.user, super.key});

  final User user;

  @override
  State<ProductsPage> createState() => _ProductsPageState();
}

class _ProductsPageState extends State<ProductsPage> {
  static const _pageSize = 4;
  final _searchController = TextEditingController();
  late final ProductService _service;
  List<Product> _products = const [];
  String? _error;
  bool _loading = true;
  int _page = 0;

  @override
  void initState() {
    super.initState();
    _service = ProductService(ApiClient());
    _load();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final products = await _service.search(
        term: _searchController.text.trim(),
        limit: 40,
      );
      if (!mounted) return;
      setState(() {
        _products = products;
        _page = 0;
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
    final totalPages = (_products.length / _pageSize).ceil().clamp(1, 999);
    final visible = _products.skip(_page * _pageSize).take(_pageSize).toList();
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(
            icon: const Icon(Icons.arrow_back_rounded),
            onPressed: () => Navigator.of(context).maybePop()),
        title: const ModuleAppTitle(
            title: 'Productos', icon: Icons.medication_rounded),
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
            title: 'Inventario',
            subtitle: '${_products.length} productos encontrados',
            icon: Icons.medication_rounded,
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _searchController,
            textInputAction: TextInputAction.search,
            onSubmitted: (_) => _load(),
            decoration: InputDecoration(
              hintText: 'Buscar producto, lote o codigo',
              prefixIcon: const Icon(Icons.search_rounded),
              suffixIcon: IconButton(
                onPressed: () {
                  _searchController.clear();
                  _load();
                },
                icon: const Icon(Icons.close_rounded),
              ),
            ),
          ),
          const SizedBox(height: 12),
          if (_error != null) ErrorPanel(message: _error!, onRetry: _load),
          if (_loading) const _InfoCard('Cargando productos...'),
          if (!_loading && _products.isEmpty)
            const _InfoCard('No hay productos para mostrar.'),
          for (final product in visible)
            _ProductCard(
              product: product,
              onAdjusted: _load,
              user: widget.user,
              service: _service,
            ),
          if (_products.isNotEmpty) ...[
            const SizedBox(height: 8),
            PagerBar(
              page: _page + 1,
              totalPages: totalPages,
              onPrevious: _page == 0 ? null : () => setState(() => _page--),
              onNext: _page >= totalPages - 1
                  ? null
                  : () => setState(() => _page++),
            ),
          ],
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

class _ProductCard extends StatelessWidget {
  const _ProductCard({
    required this.product,
    required this.user,
    required this.service,
    required this.onAdjusted,
  });
  final Product product;
  final User user;
  final ProductService service;
  final VoidCallback onAdjusted;

  @override
  Widget build(BuildContext context) {
    final color = product.stockBajo ? const Color(0xFFE67700) : brandRed;
    void openAdjust() {
      Navigator.of(context).push(MaterialPageRoute(
        builder: (_) => AdjustStockPage(
          product: product,
          user: user,
          service: service,
          onSaved: onAdjusted,
        ),
      ));
    }

    return CompactRecordCard(
      title: product.nombre,
      subtitle:
          'Stock ${product.stockActual} - vence ${product.fechaVencimiento}',
      footnote: 'Lote ${product.lote}',
      value: 'S/ ${product.precioVenta.toStringAsFixed(2)}',
      icon:
          product.stockBajo ? Icons.warning_rounded : Icons.medication_rounded,
      color: color,
      trailing: PopupMenuButton<String>(
        tooltip: 'Acciones',
        icon: const Icon(Icons.more_vert_rounded),
        onSelected: (value) {
          if (value == 'adjust') openAdjust();
        },
        itemBuilder: (_) => const [
          PopupMenuItem(
            value: 'adjust',
            child: Row(children: [
              Icon(Icons.edit_note_rounded),
              SizedBox(width: 10),
              Text('Ajustar stock'),
            ]),
          ),
        ],
      ),
      onTap: () => showModalBottomSheet<void>(
        context: context,
        showDragHandle: true,
        builder: (_) => Padding(
          padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
          child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(product.nombre,
                    style: Theme.of(context)
                        .textTheme
                        .titleLarge
                        ?.copyWith(fontWeight: FontWeight.w900)),
                const SizedBox(height: 12),
                Text('Stock actual: ${product.stockActual}'),
                Text('Stock minimo: ${product.stockMinimo}'),
                Text('Lote: ${product.lote}'),
                Text('Vencimiento: ${product.fechaVencimiento}'),
                Text('Precio: S/ ${product.precioVenta.toStringAsFixed(2)}'),
                const SizedBox(height: 16),
                FilledButton.icon(
                  onPressed: () {
                    Navigator.of(context).pop();
                    openAdjust();
                  },
                  icon: const Icon(Icons.edit_note_rounded),
                  label: const Text('Ajustar stock'),
                ),
              ]),
        ),
      ),
    );
  }
}

class AdjustStockPage extends StatefulWidget {
  const AdjustStockPage({
    required this.product,
    required this.user,
    required this.service,
    required this.onSaved,
    super.key,
  });

  final Product product;
  final User user;
  final ProductService service;
  final VoidCallback onSaved;

  @override
  State<AdjustStockPage> createState() => _AdjustStockPageState();
}

class _AdjustStockPageState extends State<AdjustStockPage> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _stockController;
  String _reason = 'Conteo fisico';
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _stockController = TextEditingController(
      text: widget.product.stockActual.toString(),
    );
  }

  @override
  void dispose() {
    _stockController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);
    try {
      await widget.service.adjustStock(
        productId: widget.product.id,
        newStock: int.parse(_stockController.text),
        reason: _reason,
        userId: widget.user.id,
      );
      widget.onSaved();
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
        content: Text('Stock ajustado correctamente'),
        behavior: SnackBarBehavior.floating,
      ));
      Navigator.of(context).pop();
    } catch (ex) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
        content: Text(ex.toString().replaceFirst('Exception: ', '')),
        behavior: SnackBarBehavior.floating,
      ));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('Ajustar stock')),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
          children: [
            CompactModuleHeader(
              title: widget.product.nombre,
              subtitle:
                  'Stock actual ${widget.product.stockActual} - Lote ${widget.product.lote}',
              icon: Icons.edit_note_rounded,
              color: brandSkyDark,
            ),
            const SizedBox(height: 14),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Form(
                  key: _formKey,
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      TextFormField(
                        controller: _stockController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'Nuevo stock',
                          prefixIcon: Icon(Icons.inventory_2_rounded),
                        ),
                        validator: (value) {
                          final parsed = int.tryParse(value ?? '');
                          if (parsed == null || parsed < 0) {
                            return 'Ingrese un stock valido';
                          }
                          return null;
                        },
                      ),
                      const SizedBox(height: 12),
                      DropdownButtonFormField<String>(
                        initialValue: _reason,
                        decoration: const InputDecoration(
                          labelText: 'Motivo',
                          prefixIcon: Icon(Icons.fact_check_rounded),
                        ),
                        items: const [
                          'Conteo fisico',
                          'Correccion de error',
                          'Producto dañado',
                          'Producto vencido',
                          'Merma',
                        ]
                            .map((value) => DropdownMenuItem(
                                  value: value,
                                  child: Text(value),
                                ))
                            .toList(),
                        onChanged: (value) => setState(() => _reason = value!),
                      ),
                      const SizedBox(height: 16),
                      FilledButton.icon(
                        onPressed: _saving ? null : _save,
                        icon: _saving
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child:
                                    CircularProgressIndicator(strokeWidth: 2),
                              )
                            : const Icon(Icons.save_rounded),
                        label: const Text('Guardar ajuste'),
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

class _InfoCard extends StatelessWidget {
  const _InfoCard(this.text);
  final String text;

  @override
  Widget build(BuildContext context) => Card(
        child: Padding(padding: const EdgeInsets.all(16), child: Text(text)),
      );
}
