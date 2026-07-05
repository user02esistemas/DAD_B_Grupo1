import 'package:flutter/foundation.dart';

import '../model/admin_user.dart';
import '../service/user_admin_service.dart';

class UserAdminViewModel extends ChangeNotifier {
  UserAdminViewModel(this._service);

  final UserAdminService _service;
  bool loading = false;
  String? error;
  List<AdminUser> users = const [];
  List<UserRole> roles = const [];

  Future<void> load() async {
    loading = true;
    error = null;
    notifyListeners();
    try {
      final data = await Future.wait([_service.users(), _service.roles()]);
      users = data[0] as List<AdminUser>;
      roles = data[1] as List<UserRole>;
    } catch (ex) {
      error = ex.toString();
    } finally {
      loading = false;
      notifyListeners();
    }
  }

  Future<void> create({required String username, required String password, required String email, required String nombreCompleto, required String dni, required String telefono, required int roleId}) async {
    await _service.create(username: username, password: password, email: email, nombreCompleto: nombreCompleto, dni: dni, telefono: telefono, roleId: roleId);
    await load();
  }

  Future<void> update(AdminUser user, {required String username, required String email, required String nombreCompleto, required String dni, required String telefono, required int roleId, required bool activo}) async {
    await _service.update(user, username: username, email: email, nombreCompleto: nombreCompleto, dni: dni, telefono: telefono, roleId: roleId, activo: activo);
    await load();
  }

  Future<void> deactivate(AdminUser user) async {
    await _service.deactivate(user);
    await load();
  }
}
