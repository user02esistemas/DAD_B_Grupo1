import '../../../core/network/api_client.dart';

class CashService {
  CashService(this._apiClient);

  final ApiClient _apiClient;

  Future<CashSession?> activeSession(int userId) async {
    final result = await _apiClient.get('/api/caja/estado', {
      'usuarioId': userId.toString(),
    });
    if (!result.success) throw Exception(result.message);
    if (result.data == null) return null;
    return CashSession.fromJson(Map<String, dynamic>.from(result.data! as Map));
  }

  Future<CashSession> openSession(
      {required int userId, required double amount}) async {
    final result = await _apiClient.post('/api/caja/abrir', {
      'usuarioId': userId,
      'cajaId': 1,
      'montoInicial': amount,
    });
    if (!result.success) throw Exception(result.message);
    return CashSession.fromJson(Map<String, dynamic>.from(result.data! as Map));
  }

  Future<void> closeSession(
      {required int userId, required double amount}) async {
    final result = await _apiClient.post('/api/caja/cerrar', {
      'usuarioId': userId,
      'montoFinal': amount,
      'observaciones': '',
    });
    if (!result.success) throw Exception(result.message);
  }

  Future<List<CashSession>> recentSessions({int limit = 2}) async {
    final result = await _apiClient.get('/api/caja/sesiones', {
      'limite': limit.toString(),
    });
    if (!result.success) throw Exception(result.message);
    return result.data is List
        ? (result.data! as List)
            .whereType<Map>()
            .map(
                (json) => CashSession.fromJson(Map<String, dynamic>.from(json)))
            .toList()
        : const [];
  }
}

class CashSession {
  const CashSession({
    required this.id,
    required this.cajaNombre,
    required this.montoInicial,
    required this.totalVentasEfectivo,
    required this.totalVentasVirtual,
    required this.efectivoEsperado,
  });

  final int id;
  final String cajaNombre;
  final double montoInicial;
  final double totalVentasEfectivo;
  final double totalVentasVirtual;
  final double efectivoEsperado;

  factory CashSession.fromJson(Map<String, dynamic> json) => CashSession(
        id: (json['id'] as num?)?.toInt() ?? 0,
        cajaNombre: json['cajaNombre']?.toString() ?? 'Caja principal',
        montoInicial: _double(json['montoInicial']),
        totalVentasEfectivo: _double(json['totalVentasEfectivo']),
        totalVentasVirtual: _double(json['totalVentasVirtual']),
        efectivoEsperado: _double(json['efectivoEsperado']),
      );

  static double _double(Object? value) =>
      (value as num?)?.toDouble() ??
      double.tryParse(value?.toString() ?? '') ??
      0;
}
