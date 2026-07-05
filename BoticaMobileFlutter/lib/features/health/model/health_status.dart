class HealthStatus {
  const HealthStatus({
    required this.api,
    required this.rmi,
    required this.message,
    required this.missingServices,
  });

  final String api;
  final String rmi;
  final String message;
  final List<String> missingServices;

  bool get isHealthy => api == 'UP' && rmi == 'UP' && missingServices.isEmpty;

  factory HealthStatus.fromResult(String message, Object? data) {
    if (data is! Map<String, dynamic>) {
      return HealthStatus(api: 'UNKNOWN', rmi: 'UNKNOWN', message: message, missingServices: const []);
    }

    final missing = data['missingServices'];
    return HealthStatus(
      api: data['api']?.toString() ?? 'UNKNOWN',
      rmi: data['rmi']?.toString() ?? 'UNKNOWN',
      message: message,
      missingServices: missing is List ? missing.map((item) => item.toString()).toList() : const [],
    );
  }
}
