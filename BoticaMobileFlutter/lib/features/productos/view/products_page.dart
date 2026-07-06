import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../../../core/widgets/mobile_module_widgets.dart';
import '../model/product.dart';
import '../service/product_service.dart';

class ProductsPage extends StatefulWidget {
  const ProductsPage({super.key});

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
        title: const Text('Productos'),
        actions: [
          IconButton(onPressed: _load, icon: const Icon(Icons.refresh_rounded))
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 28),
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
          for (final product in visible) _ProductCard(product: product),
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
}

class _ProductCard extends StatelessWidget {
  const _ProductCard({required this.product});
  final Product product;

  @override
  Widget build(BuildContext context) {
    final color = product.stockBajo ? const Color(0xFFE67700) : brandRed;
    return CompactRecordCard(
      title: product.nombre,
      subtitle:
          'Stock ${product.stockActual} - vence ${product.fechaVencimiento}',
      footnote: 'Lote ${product.lote}',
      value: 'S/ ${product.precioVenta.toStringAsFixed(2)}',
      icon:
          product.stockBajo ? Icons.warning_rounded : Icons.medication_rounded,
      color: color,
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
              ]),
        ),
      ),
    );
  }
}

class _InfoCard extends StatelessWidget {
  const _InfoCard(this.text);
  final String text;

  @override
  Widget build(BuildContext context) => Card(
        child: Padding(padding: const EdgeInsets.all(16), child: Text(text)),
      );
}
