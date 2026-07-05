class User {
  const User({
    required this.id,
    required this.username,
    required this.nombreCompleto,
  });

  final int id;
  final String username;
  final String nombreCompleto;

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: (json['id'] as num?)?.toInt() ?? 0,
      username: json['username']?.toString() ?? '',
      nombreCompleto: json['nombreCompleto']?.toString() ?? 'Usuario',
    );
  }
}
