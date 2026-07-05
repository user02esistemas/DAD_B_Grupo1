import '../../core/network/api_client.dart';
import 'product.dart';

class ProductService {
  ProductService(this._apiClient);

  final ApiClient _apiClient;

  Future<List<Product>> search({String? term, int limit = 20}) async {
    final result = await _apiClient.get('/api/productos', {
      'termino': term,
      'limite': limit.toString(),
    });

    if (!result.success || result.data is! List) {
      throw Exception(result.message);
    }

    return (result.data! as List)
        .whereType<Map<String, dynamic>>()
        .map(Product.fromJson)
        .toList();
  }
}
