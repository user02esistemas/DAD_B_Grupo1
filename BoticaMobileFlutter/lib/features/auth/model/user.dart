class User {
  const User({
    required this.id,
    required this.username,
    required this.nombreCompleto,
    required this.roles,
  });

  final int id;
  final String username;
  final String nombreCompleto;
  final List<Role> roles;

  bool get isAdmin => hasRole('ADMIN');
  bool get isFarmaceutico => hasRole('FARMACEUTICO');
  bool get isAlmacenero => hasRole('ALMACENERO');

  bool get canViewDashboard => roles.isNotEmpty;
  bool get canViewProductos => isAdmin || isFarmaceutico || isAlmacenero;
  bool get canViewVentas => isAdmin || isFarmaceutico;
  bool get canViewCompras => isAdmin || isAlmacenero;
  bool get canViewCaja => isAdmin || isFarmaceutico;
  bool get canViewReportes => isAdmin || isFarmaceutico || isAlmacenero;
  bool get canViewUsuarios => isAdmin;

  bool hasRole(String roleName) => roles.any((role) => role.normalizedName == roleName);

  String get rolesLabel {
    if (roles.isEmpty) {
      return 'Sin rol asignado';
    }
    return roles.map((role) => role.normalizedName).join(', ');
  }

  factory User.fromJson(Map<String, dynamic> json) {
    final rawRoles = json['roles'];
    return User(
      id: (json['id'] as num?)?.toInt() ?? 0,
      username: json['username']?.toString() ?? '',
      nombreCompleto: json['nombreCompleto']?.toString() ?? 'Usuario',
      roles: rawRoles is List
          ? rawRoles.whereType<Map<String, dynamic>>().map(Role.fromJson).toList()
          : const [],
    );
  }
}

class Role {
  const Role({required this.id, required this.nombre, required this.descripcion});

  final int id;
  final String nombre;
  final String descripcion;

  String get normalizedName => nombre.replaceFirst('ROLE_', '');

  factory Role.fromJson(Map<String, dynamic> json) {
    return Role(
      id: (json['id'] as num?)?.toInt() ?? 0,
      nombre: json['nombre']?.toString() ?? '',
      descripcion: json['descripcion']?.toString() ?? '',
    );
  }
}
