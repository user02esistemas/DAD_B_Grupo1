import '../../core/network/api_client.dart';
import 'user.dart';

class AuthService {
  AuthService(this._apiClient);

  final ApiClient _apiClient;

  Future<User> login(String username, String password) async {
    final result = await _apiClient.post('/api/auth/login', {
      'username': username,
      'password': password,
    });

    if (!result.success || result.data is! Map<String, dynamic>) {
      throw Exception(result.message);
    }

    return User.fromJson(result.data! as Map<String, dynamic>);
  }
}
