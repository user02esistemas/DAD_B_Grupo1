import 'package:flutter/foundation.dart';

import '../model/health_status.dart';
import '../service/health_service.dart';

class HealthViewModel extends ChangeNotifier {
  HealthViewModel(this._healthService);

  final HealthService _healthService;

  bool _loading = false;
  bool _failed = false;
  HealthStatus? _status;

  bool get loading => _loading;
  bool get failed => _failed;
  HealthStatus? get status => _status;

  Future<void> check() async {
    _loading = true;
    _failed = false;
    notifyListeners();

    try {
      _status = await _healthService.check();
    } catch (_) {
      _failed = true;
    } finally {
      _loading = false;
      notifyListeners();
    }
  }
}
