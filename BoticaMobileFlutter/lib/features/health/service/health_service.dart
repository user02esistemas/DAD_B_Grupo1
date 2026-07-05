import '../../../core/network/api_client.dart';
import '../model/health_status.dart';

class HealthService {
  HealthService(this._apiClient);

  final ApiClient _apiClient;

  Future<HealthStatus> check() async {
    final result = await _apiClient.get('/api/health');
    return HealthStatus.fromResult(result.message, result.data);
  }
}
