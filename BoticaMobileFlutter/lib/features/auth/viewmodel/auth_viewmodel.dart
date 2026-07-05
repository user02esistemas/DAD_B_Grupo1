import 'package:flutter/foundation.dart';

import '../model/user.dart';
import '../service/auth_service.dart';

class AuthViewModel extends ChangeNotifier {
  AuthViewModel(this._authService);

  final AuthService _authService;

  bool _loading = false;
  String? _error;

  bool get loading => _loading;
  String? get error => _error;

  Future<User?> login(String username, String password) async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      return await _authService.login(username, password);
    } catch (ex) {
      _error = ex.toString().replaceFirst('Exception: ', '');
      return null;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }
}
