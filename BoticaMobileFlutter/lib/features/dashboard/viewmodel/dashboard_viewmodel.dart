import 'package:flutter/foundation.dart';

import '../model/dashboard_summary.dart';
import '../service/dashboard_service.dart';

class DashboardViewModel extends ChangeNotifier {
  DashboardViewModel(this._dashboardService);

  final DashboardService _dashboardService;

  bool _loading = false;
  String? _error;
  DashboardSummary? _summary;

  bool get loading => _loading;
  String? get error => _error;
  DashboardSummary? get summary => _summary;

  Future<void> load() async {
    _loading = true;
    _error = null;
    notifyListeners();

    try {
      _summary = await _dashboardService.getSummary();
    } catch (ex) {
      _error = ex.toString();
    } finally {
      _loading = false;
      notifyListeners();
    }
  }
}
