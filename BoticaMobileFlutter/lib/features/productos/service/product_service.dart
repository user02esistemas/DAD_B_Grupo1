import '../../../core/network/api_client.dart';
import '../model/product.dart';

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
        .whereType<Map>()
        .map((json) => Product.fromJson(Map<String, dynamic>.from(json)))
        .toList();
  }

  Future<void> adjustStock({
    required int productId,
    required int newStock,
    required String reason,
    required int userId,
  }) async {
    final result = await _apiClient.post('/api/inventario/ajustar', {
      'productoId': productId,
      'nuevoStock': newStock,
      'motivo': reason,
      'usuarioId': userId,
    });

    if (!result.success) {
      throw Exception(result.message);
    }
  }
}
