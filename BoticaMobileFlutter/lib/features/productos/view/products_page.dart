import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../model/product.dart';
import '../service/product_service.dart';
import '../viewmodel/product_viewmodel.dart';

class ProductsPage extends StatefulWidget {
  const ProductsPage({super.key});

  @override
  State<ProductsPage> createState() => _ProductsPageState();
}

class _ProductsPageState extends State<ProductsPage> {
  final _searchController = TextEditingController();
  late final ProductViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = ProductViewModel(ProductService(ApiClient()))..addListener(_onViewModelChanged);
    _reload();
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    _searchController.dispose();
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) setState(() {});
  }

  void _reload() => _viewModel.search(term: _searchController.text.trim(), limit: 20);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()),
        title: const Text('Productos'),
      ),
      floatingActionButton: FloatingActionButton.extended(onPressed: _reload, icon: const Icon(Icons.refresh_rounded), label: const Text('Actualizar')),
      body: RefreshIndicator(onRefresh: () => _viewModel.search(term: _searchController.text.trim(), limit: 20), child: _buildBody()),
    );
  }

  Widget _buildBody() {
    final products = _viewModel.products;
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(16, 6, 16, 90),
      children: [
        _InventoryHeader(controller: _searchController, loading: _viewModel.loading, onSearch: _reload, products: products),
        const SizedBox(height: 10),
        if (_viewModel.loading) const _LoadingProductsCard(),
        if (!_viewModel.loading && _viewModel.error != null) ErrorPanel(message: _viewModel.error!, onRetry: _reload),
        if (!_viewModel.loading && _viewModel.error == null && products.isEmpty) const _EmptyProductsCard(),
        if (!_viewModel.loading && products.isNotEmpty) ...[
          Text('Inventario disponible', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 10),
          for (final product in products) ...[
            _ProductCard(product: product),
            const SizedBox(height: 10),
          ],
        ],
      ],
    );
  }
}

class _InventoryHeader extends StatelessWidget {
  const _InventoryHeader({required this.controller, required this.loading, required this.onSearch, required this.products});

  final TextEditingController controller;
  final bool loading;
  final VoidCallback onSearch;
  final List<Product> products;

  @override
  Widget build(BuildContext context) {
    final lowStock = products.where((product) => product.stockBajo).length;
    final totalStock = products.fold<int>(0, (sum, product) => sum + product.stockActual);
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 6, 16, 14),
      child: Column(
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(borderRadius: BorderRadius.circular(26), gradient: const LinearGradient(colors: [Color(0xFF087B68), Color(0xFF15B89C)])),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(children: [Expanded(child: Text('Inventario activo', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w900))), const Icon(Icons.inventory_rounded, color: Colors.white)]),
                const SizedBox(height: 12),
                Wrap(spacing: 8, runSpacing: 8, children: [_HeaderStat(label: 'Productos', value: products.length.toString()), _HeaderStat(label: 'Unidades', value: totalStock.toString()), _HeaderStat(label: 'Stock bajo', value: lowStock.toString())]),
              ],
            ),
          ),
          const SizedBox(height: 12),
          Row(children: [Expanded(child: TextField(controller: controller, decoration: const InputDecoration(prefixIcon: Icon(Icons.search_rounded), labelText: 'Buscar medicamento'), onSubmitted: (_) => onSearch())), const SizedBox(width: 10), FilledButton(onPressed: loading ? null : onSearch, child: const Text('Buscar'))]),
        ],
      ),
    );
  }
}

class _EmptyProductsCard extends StatelessWidget {
  const _EmptyProductsCard();

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          children: [
            Container(
              width: 58,
              height: 58,
              decoration: BoxDecoration(color: const Color(0xFF087B68).withValues(alpha: 0.10), borderRadius: BorderRadius.circular(20)),
              child: const Icon(Icons.search_off_rounded, color: Color(0xFF087B68), size: 30),
            ),
            const SizedBox(height: 12),
            Text('Sin productos para mostrar', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)),
            const SizedBox(height: 4),
            const Text('Busque por nombre, principio activo o lote.', textAlign: TextAlign.center, style: TextStyle(color: Colors.black54)),
          ],
        ),
      ),
    );
  }
}

class _LoadingProductsCard extends StatelessWidget {
  const _LoadingProductsCard();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: Padding(
        padding: EdgeInsets.all(18),
        child: Row(
          children: [
            SizedBox(width: 22, height: 22, child: CircularProgressIndicator(strokeWidth: 2.4)),
            SizedBox(width: 14),
            Expanded(child: Text('Cargando inventario...')),
          ],
        ),
      ),
    );
  }
}

class _ProductCard extends StatelessWidget {
  const _ProductCard({required this.product});

  final Product product;

  @override
  Widget build(BuildContext context) {
    final color = product.stockBajo ? const Color(0xFFE03131) : const Color(0xFF087B68);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(children: [
          Container(width: 54, height: 54, decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(18)), child: Icon(Icons.medication_liquid_rounded, color: color, size: 29)),
          const SizedBox(width: 12),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(product.nombre, maxLines: 2, overflow: TextOverflow.ellipsis, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)), const SizedBox(height: 5), Text('Lote ${product.lote} - Vence ${product.fechaVencimiento}', style: const TextStyle(color: Colors.black54, fontSize: 12)), const SizedBox(height: 8), Wrap(spacing: 7, runSpacing: 7, children: [_ProductPill(label: 'Stock ${product.stockActual}', color: color), _ProductPill(label: 'Min ${product.stockMinimo}', color: const Color(0xFF1C7ED6))])])),
          const SizedBox(width: 8),
          Column(crossAxisAlignment: CrossAxisAlignment.end, children: [Text('S/ ${product.precioVenta.toStringAsFixed(2)}', style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w900)), const SizedBox(height: 10), OutlinedButton(onPressed: () {}, child: const Text('Detalle'))]),
        ]),
      ),
    );
  }
}

class _HeaderStat extends StatelessWidget {
  const _HeaderStat({required this.label, required this.value});
  final String label;
  final String value;
  @override
  Widget build(BuildContext context) => SizedBox(width: 108, child: Container(padding: const EdgeInsets.all(10), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.16), borderRadius: BorderRadius.circular(16)), child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(value, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w900, fontSize: 18)), Text(label, style: const TextStyle(color: Colors.white70, fontSize: 11))])));
}

class _ProductPill extends StatelessWidget {
  const _ProductPill({required this.label, required this.color});
  final String label;
  final Color color;
  @override
  Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 9, vertical: 5), decoration: BoxDecoration(color: color.withValues(alpha: 0.10), borderRadius: BorderRadius.circular(999)), child: Text(label, style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w800)));
}
