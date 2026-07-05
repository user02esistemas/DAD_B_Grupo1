import 'package:flutter/material.dart';

import '../../core/network/api_client.dart';
import '../../core/widgets/error_panel.dart';
import 'product.dart';
import 'product_service.dart';

class ProductsPage extends StatefulWidget {
  const ProductsPage({super.key});

  @override
  State<ProductsPage> createState() => _ProductsPageState();
}

class _ProductsPageState extends State<ProductsPage> {
  final _searchController = TextEditingController(text: 'para');
  late final ProductService _productService;
  late Future<List<Product>> _productsFuture;

  @override
  void initState() {
    super.initState();
    _productService = ProductService(ApiClient());
    _productsFuture = _search();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<List<Product>> _search() {
    return _productService.search(term: _searchController.text.trim(), limit: 20);
  }

  void _reload() {
    setState(() => _productsFuture = _search());
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
                FilledButton(onPressed: _reload, child: const Text('Buscar')),
              ],
            ),
          ),
          Expanded(
            child: FutureBuilder<List<Product>>(
              future: _productsFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }
                if (snapshot.hasError) {
                  return ErrorPanel(message: snapshot.error.toString(), onRetry: _reload);
                }
                final products = snapshot.data ?? const [];
                if (products.isEmpty) {
                  return const Center(child: Text('No se encontraron productos'));
                }
                return ListView.separated(
                  padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                  itemCount: products.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 8),
                  itemBuilder: (context, index) => _ProductTile(product: products[index]),
                );
              },
            ),
          ),
        ],
      ),
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
