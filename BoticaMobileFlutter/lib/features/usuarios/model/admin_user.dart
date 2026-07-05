class AdminUser {
  const AdminUser({required this.id, required this.username, required this.email, required this.nombreCompleto, required this.dni, required this.telefono, required this.activo, required this.roles});

  final int id;
  final String username;
  final String email;
  final String nombreCompleto;
  final String dni;
  final String telefono;
  final bool activo;
  final List<UserRole> roles;

  String get roleLabel => roles.isEmpty ? 'Sin rol' : roles.map((role) => role.cleanName).join(', ');

  factory AdminUser.fromJson(Map<String, dynamic> json) {
    final rawRoles = json['roles'];
    return AdminUser(
      id: (json['id'] as num?)?.toInt() ?? 0,
      username: json['username']?.toString() ?? '',
      email: json['email']?.toString() ?? '',
      nombreCompleto: json['nombreCompleto']?.toString() ?? '',
      dni: json['dni']?.toString() ?? '',
      telefono: json['telefono']?.toString() ?? '',
      activo: json['activo'] == true,
      roles: rawRoles is List ? rawRoles.whereType<Map<String, dynamic>>().map(UserRole.fromJson).toList() : const [],
    );
  }
}

class UserRole {
  const UserRole({required this.id, required this.nombre, required this.descripcion});

  final int id;
  final String nombre;
  final String descripcion;

  String get cleanName => nombre.replaceFirst('ROLE_', '');

  factory UserRole.fromJson(Map<String, dynamic> json) {
    return UserRole(
      id: (json['id'] as num?)?.toInt() ?? 0,
      nombre: json['nombre']?.toString() ?? '',
      descripcion: json['descripcion']?.toString() ?? '',
    );
  }
}
