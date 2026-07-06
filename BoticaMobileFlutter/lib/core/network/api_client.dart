import 'dart:convert';

import 'package:http/http.dart' as http;

import '../config/app_config.dart';

class ApiClient {
  ApiClient({http.Client? httpClient})
      : _httpClient = httpClient ?? http.Client();

  final http.Client _httpClient;

  Future<ApiResult> get(String path,
      [Map<String, String?> query = const {}]) async {
    final response = await _httpClient.get(_uri(path, query));
    return _parse(response);
  }

  Future<ApiResult> post(String path, Map<String, Object?> body) async {
    final response = await _httpClient.post(
      _uri(path),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(body),
    );
    return _parse(response);
  }

  Future<ApiResult> put(String path, Map<String, Object?> body) async {
    final response = await _httpClient.put(
      _uri(path),
      headers: const {'Content-Type': 'application/json'},
      body: jsonEncode(body),
    );
    return _parse(response);
  }

  Future<ApiResult> delete(String path) async {
    final response = await _httpClient.delete(_uri(path));
    return _parse(response);
  }

  Uri _uri(String path, [Map<String, String?> query = const {}]) {
    final base = Uri.parse(AppConfig.apiBaseUrl);
    final cleanQuery = <String, String>{};
    for (final entry in query.entries) {
      final value = entry.value;
      if (value != null && value.isNotEmpty) {
        cleanQuery[entry.key] = value;
      }
    }

    return base.replace(
      path: '${base.path}${path.startsWith('/') ? path : '/$path'}',
      queryParameters: cleanQuery.isEmpty ? null : cleanQuery,
    );
  }

  ApiResult _parse(http.Response response) {
    final Map<String, dynamic> decoded;
    try {
      decoded = jsonDecode(response.body) as Map<String, dynamic>;
    } on FormatException {
      return ApiResult(
        success: false,
        statusCode: response.statusCode,
        message: response.statusCode == 404
            ? 'No se encontro el servicio. Verifique la direccion del servidor.'
            : 'El servidor devolvio una respuesta no valida.',
        data: null,
      );
    }
    final success = decoded['success'] == true && response.statusCode < 400;
    return ApiResult(
      success: success,
      statusCode: response.statusCode,
      message: decoded['message']?.toString() ?? 'Respuesta sin mensaje',
      data: decoded['data'],
    );
  }
}

class ApiResult {
  const ApiResult({
    required this.success,
    required this.statusCode,
    required this.message,
    required this.data,
  });

  final bool success;
  final int statusCode;
  final String message;
  final Object? data;
}
