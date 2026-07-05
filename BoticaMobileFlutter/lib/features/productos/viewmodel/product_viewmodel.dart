import 'package:flutter/foundation.dart';

import '../model/product.dart';
import '../service/product_service.dart';

class ProductViewModel extends ChangeNotifier {
  ProductViewModel(this._productService);

  final ProductService _productService;

  bool _loading = false;
  String? _error;
  List<Product> _products = const [];

  bool get loading => _loading;
  String? get error => _error;
  List<Product> get products => _products;

  Future<void> search({String? term, int limit = 20}) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _products = await _productService.search(term: term, limit: limit);
    } catch (ex) {
      _error = ex.toString();
    } finally {
      _loading = false;
      notifyListeners();
    }
  }
}
