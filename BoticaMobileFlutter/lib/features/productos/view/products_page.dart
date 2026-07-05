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
  final _searchController = TextEditingController(text: 'para');
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
    if (mounted) {
      setState(() {});
    }
  }

  void _reload() {
    _viewModel.search(term: _searchController.text.trim(), limit: 20);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Productos')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: _searchController,
                    decoration: const InputDecoration(labelText: 'Buscar producto'),
                    onSubmitted: (_) => _reload(),
                  ),
                ),
                const SizedBox(width: 10),
                FilledButton(onPressed: _viewModel.loading ? null : _reload, child: const Text('Buscar')),
              ],
            ),
          ),
          Expanded(child: _buildBody()),
        ],
      ),
    );
  }

  Widget _buildBody() {
    if (_viewModel.loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_viewModel.error != null) {
      return ErrorPanel(message: _viewModel.error!, onRetry: _reload);
    }
    if (_viewModel.products.isEmpty) {
      return const Center(child: Text('No se encontraron productos'));
    }
    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
      itemCount: _viewModel.products.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (context, index) => _ProductTile(product: _viewModel.products[index]),
    );
  }
}

class _ProductTile extends StatelessWidget {
  const _ProductTile({required this.product});

  final Product product;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: Text(product.nombre),
        subtitle: Text('Lote ${product.lote} - Vence ${product.fechaVencimiento}'),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text('S/ ${product.precioVenta.toStringAsFixed(2)}'),
            Text(
              'Stock: ${product.stockActual}',
              style: TextStyle(color: product.stockBajo ? Theme.of(context).colorScheme.error : null),
            ),
          ],
        ),
      ),
    );
  }
}
