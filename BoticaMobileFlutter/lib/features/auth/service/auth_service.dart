import '../../../core/network/api_client.dart';
import '../model/user.dart';

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

  Future<void> logout(String username) async {
    final result = await _apiClient.post('/api/auth/logout', {
      'username': username,
    });

    if (!result.success) {
      throw Exception(result.message);
    }
  }
}
