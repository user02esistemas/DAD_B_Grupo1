import '../../../core/network/api_client.dart';
import '../model/admin_user.dart';

class UserAdminService {
  UserAdminService(this._apiClient);

  final ApiClient _apiClient;

  Future<List<AdminUser>> users() async {
    final result = await _apiClient.get('/api/usuarios');
    if (!result.success || result.data is! List) {
      throw Exception(result.message);
    }
    return (result.data! as List)
        .whereType<Map<String, dynamic>>()
        .map(AdminUser.fromJson)
        .toList();
  }

  Future<AdminUser> userById(int id) async {
    final result = await _apiClient.get('/api/usuarios/$id');
    if (!result.success || result.data is! Map) throw Exception(result.message);
    return AdminUser.fromJson(Map<String, dynamic>.from(result.data! as Map));
  }

  Future<List<UserRole>> roles() async {
    final result = await _apiClient.get('/api/usuarios/roles');
    if (!result.success || result.data is! List) {
      throw Exception(result.message);
    }
    return (result.data! as List)
        .whereType<Map<String, dynamic>>()
        .map(UserRole.fromJson)
        .toList();
  }

  Future<void> create(
      {required String username,
      required String password,
      required String email,
      required String nombreCompleto,
      required String dni,
      required String telefono,
      required int roleId}) async {
    final result = await _apiClient.post('/api/usuarios', {
      'username': username,
      'password': password,
      'email': email,
      'nombreCompleto': nombreCompleto,
      'dni': dni,
      'telefono': telefono,
      'activo': true,
      'rolesIds': [roleId],
    });
    if (!result.success) throw Exception(result.message);
  }

  Future<void> update(AdminUser user,
      {required String username,
      required String email,
      required String nombreCompleto,
      required String dni,
      required String telefono,
      required int roleId,
      required bool activo}) async {
    final result = await _apiClient.put('/api/usuarios/${user.id}', {
      'username': username,
      'email': email,
      'nombreCompleto': nombreCompleto,
      'dni': dni,
      'telefono': telefono,
      'activo': activo,
      'rolesIds': [roleId],
    });
    if (!result.success) throw Exception(result.message);
  }

  Future<void> changePassword(AdminUser user, String password) async {
    final result = await _apiClient.put('/api/usuarios/${user.id}/password', {
      'password': password,
    });
    if (!result.success) throw Exception(result.message);
  }

  Future<void> deactivate(AdminUser user) async {
    final result = await _apiClient.delete('/api/usuarios/${user.id}');
    if (!result.success) throw Exception(result.message);
  }
}
