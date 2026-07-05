import '../../../core/network/api_client.dart';
import '../model/dashboard_summary.dart';

class DashboardService {
  DashboardService(this._apiClient);

  final ApiClient _apiClient;

  Future<DashboardSummary> getSummary() async {
    final result = await _apiClient.get('/api/dashboard/resumen');
    if (!result.success || result.data is! Map<String, dynamic>) {
      throw Exception(result.message);
    }
    return DashboardSummary.fromJson(result.data! as Map<String, dynamic>);
  }
}
