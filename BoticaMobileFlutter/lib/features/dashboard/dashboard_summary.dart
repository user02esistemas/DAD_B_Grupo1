class DashboardSummary {
  const DashboardSummary({
    required this.ventasHoy,
    required this.cantidadVentasHoy,
    required this.ventasMes,
    required this.comprasHoy,
    required this.totalProductos,
    required this.stockBajo,
    required this.agotados,
    required this.porVencer,
    required this.vencidos,
  });

  final double ventasHoy;
  final int cantidadVentasHoy;
  final double ventasMes;
  final double comprasHoy;
  final int totalProductos;
  final int stockBajo;
  final int agotados;
  final int porVencer;
  final int vencidos;

  factory DashboardSummary.fromJson(Map<String, dynamic> json) {
    return DashboardSummary(
      ventasHoy: _double(json['ventasHoy']),
      cantidadVentasHoy: _int(json['cantidadVentasHoy']),
      ventasMes: _double(json['ventasMes']),
      comprasHoy: _double(json['comprasHoy']),
      totalProductos: _int(json['totalProductos']),
      stockBajo: _int(json['stockBajo']),
      agotados: _int(json['agotados']),
      porVencer: _int(json['porVencer']),
      vencidos: _int(json['vencidos']),
    );
  }

  static double _double(Object? value) => value is num ? value.toDouble() : double.tryParse(value?.toString() ?? '') ?? 0;
  static int _int(Object? value) => value is num ? value.toInt() : int.tryParse(value?.toString() ?? '') ?? 0;
}
