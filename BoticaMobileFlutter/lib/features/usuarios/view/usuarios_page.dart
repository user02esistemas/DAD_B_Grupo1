import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../../core/widgets/error_panel.dart';
import '../model/admin_user.dart';
import '../service/user_admin_service.dart';
import '../viewmodel/user_admin_viewmodel.dart';

class UsuariosPage extends StatefulWidget {
  const UsuariosPage({super.key});

  @override
  State<UsuariosPage> createState() => _UsuariosPageState();
}

class _UsuariosPageState extends State<UsuariosPage> {
  late final UserAdminViewModel _viewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = UserAdminViewModel(UserAdminService(ApiClient()))..addListener(_onChanged);
    _viewModel.load();
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onChanged);
    super.dispose();
  }

  void _onChanged() {
    if (mounted) setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        leading: IconButton(icon: const Icon(Icons.arrow_back_rounded), onPressed: () => Navigator.of(context).maybePop()),
        title: const Text('Usuarios'),
        actions: [IconButton(onPressed: _viewModel.load, icon: const Icon(Icons.refresh_rounded))],
      ),
      floatingActionButton: FloatingActionButton.extended(onPressed: _viewModel.roles.isEmpty ? null : () => _openEditor(), icon: const Icon(Icons.person_add_alt_rounded), label: const Text('Nuevo')),
      body: RefreshIndicator(onRefresh: _viewModel.load, child: _buildBody()),
    );
  }

  Widget _buildBody() {
    if (_viewModel.loading && _viewModel.users.isEmpty) {
      return ListView(padding: const EdgeInsets.all(18), children: const [_UsersHero(total: 0, active: 0), SizedBox(height: 14), _LoadingCard()]);
    }
    if (_viewModel.error != null && _viewModel.users.isEmpty) {
      return ListView(padding: const EdgeInsets.all(18), children: [const _UsersHero(total: 0, active: 0), const SizedBox(height: 14), ErrorPanel(message: _viewModel.error!, onRetry: _viewModel.load)]);
    }

    final active = _viewModel.users.where((user) => user.activo).length;
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      padding: const EdgeInsets.fromLTRB(18, 8, 18, 96),
      children: [
        _UsersHero(total: _viewModel.users.length, active: active),
        const SizedBox(height: 18),
        Text('Personal registrado', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
        const SizedBox(height: 10),
        if (_viewModel.users.isEmpty) const _EmptyUsersCard(),
        for (final user in _viewModel.users) _UserCard(user: user, onEdit: () => _openEditor(user: user), onDeactivate: user.activo ? () => _deactivate(user) : null),
      ],
    );
  }

  Future<void> _deactivate(AdminUser user) async {
    await _viewModel.deactivate(user);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Usuario desactivado')));
  }

  Future<void> _openEditor({AdminUser? user}) async {
    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      builder: (_) => _UserEditorSheet(viewModel: _viewModel, user: user),
    );
  }
}

class _UsersHero extends StatelessWidget {
  const _UsersHero({required this.total, required this.active});
  final int total;
  final int active;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(borderRadius: BorderRadius.circular(30), gradient: const LinearGradient(colors: [Color(0xFF364FC7), Color(0xFF748FFC)])),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Container(width: 56, height: 56, decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(18)), child: const Icon(Icons.group_rounded, color: Colors.white, size: 32)),
        const SizedBox(height: 18),
        Text('Usuarios', style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
        const SizedBox(height: 6),
        const Text('Administra personal, acceso y roles.', style: TextStyle(color: Colors.white70)),
        const SizedBox(height: 14),
        Wrap(spacing: 8, runSpacing: 8, children: [_HeroPill('$total registrados'), _HeroPill('$active activos')]),
      ]),
    );
  }
}

class _UserCard extends StatelessWidget {
  const _UserCard({required this.user, required this.onEdit, required this.onDeactivate});
  final AdminUser user;
  final VoidCallback onEdit;
  final VoidCallback? onDeactivate;

  @override
  Widget build(BuildContext context) {
    final color = user.activo ? const Color(0xFF364FC7) : Colors.black38;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(children: [
          CircleAvatar(backgroundColor: color.withValues(alpha: 0.12), child: Icon(Icons.badge_rounded, color: color)),
          const SizedBox(width: 12),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text(user.nombreCompleto, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(fontWeight: FontWeight.w900)), Text(user.email, maxLines: 1, overflow: TextOverflow.ellipsis, style: const TextStyle(color: Colors.black54)), Text(user.roleLabel, style: TextStyle(color: color, fontWeight: FontWeight.w800, fontSize: 12))])),
          PopupMenuButton<String>(
            onSelected: (value) {
              if (value == 'edit') onEdit();
              if (value == 'off') onDeactivate?.call();
            },
            itemBuilder: (_) => [
              const PopupMenuItem(value: 'edit', child: Text('Editar')),
              if (onDeactivate != null) const PopupMenuItem(value: 'off', child: Text('Desactivar')),
            ],
          ),
        ]),
      ),
    );
  }
}

class _UserEditorSheet extends StatefulWidget {
  const _UserEditorSheet({required this.viewModel, this.user});
  final UserAdminViewModel viewModel;
  final AdminUser? user;

  @override
  State<_UserEditorSheet> createState() => _UserEditorSheetState();
}

class _UserEditorSheetState extends State<_UserEditorSheet> {
  final _formKey = GlobalKey<FormState>();
  late final TextEditingController _username;
  late final TextEditingController _password;
  late final TextEditingController _email;
  late final TextEditingController _name;
  late final TextEditingController _dni;
  late final TextEditingController _phone;
  late bool _active;
  int? _roleId;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    final user = widget.user;
    _username = TextEditingController(text: user?.username ?? '');
    _password = TextEditingController();
    _email = TextEditingController(text: user?.email ?? '');
    _name = TextEditingController(text: user?.nombreCompleto ?? '');
    _dni = TextEditingController(text: user?.dni ?? '');
    _phone = TextEditingController(text: user?.telefono ?? '');
    _active = user?.activo ?? true;
    _roleId = user?.roles.isNotEmpty == true ? user!.roles.first.id : (widget.viewModel.roles.isNotEmpty ? widget.viewModel.roles.first.id : null);
  }

  @override
  void dispose() {
    _username.dispose();
    _password.dispose();
    _email.dispose();
    _name.dispose();
    _dni.dispose();
    _phone.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final bottom = MediaQuery.viewInsetsOf(context).bottom;
    final editing = widget.user != null;
    return Padding(
      padding: EdgeInsets.fromLTRB(18, 18, 18, bottom + 18),
      child: Form(
        key: _formKey,
        child: ListView(shrinkWrap: true, children: [
          Text(editing ? 'Editar usuario' : 'Nuevo usuario', style: Theme.of(context).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.w900)),
          const SizedBox(height: 14),
          _Field(controller: _name, label: 'Nombre completo', icon: Icons.person_rounded),
          _Field(controller: _username, label: 'Usuario', icon: Icons.account_circle_rounded),
          if (!editing) _Field(controller: _password, label: 'Clave inicial', icon: Icons.lock_rounded, obscure: true),
          _Field(controller: _email, label: 'Correo', icon: Icons.mail_rounded),
          Row(children: [Expanded(child: _Field(controller: _dni, label: 'DNI', icon: Icons.credit_card_rounded)), const SizedBox(width: 10), Expanded(child: _Field(controller: _phone, label: 'Telefono', icon: Icons.phone_rounded))]),
          const SizedBox(height: 8),
          DropdownButtonFormField<int>(
            initialValue: _roleId,
            decoration: const InputDecoration(prefixIcon: Icon(Icons.admin_panel_settings_rounded), labelText: 'Rol'),
            items: widget.viewModel.roles.map((role) => DropdownMenuItem(value: role.id, child: Text(role.cleanName))).toList(),
            onChanged: (value) => setState(() => _roleId = value),
            validator: (value) => value == null ? 'Seleccione rol' : null,
          ),
          if (editing) SwitchListTile(value: _active, onChanged: (value) => setState(() => _active = value), title: const Text('Usuario activo')),
          const SizedBox(height: 12),
          FilledButton.icon(onPressed: _saving ? null : _save, icon: _saving ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2)) : const Icon(Icons.save_rounded), label: Text(editing ? 'Guardar cambios' : 'Crear usuario')),
        ]),
      ),
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate() || _roleId == null) return;
    setState(() => _saving = true);
    try {
      final user = widget.user;
      if (user == null) {
        await widget.viewModel.create(username: _username.text.trim(), password: _password.text, email: _email.text.trim(), nombreCompleto: _name.text.trim(), dni: _dni.text.trim(), telefono: _phone.text.trim(), roleId: _roleId!);
      } else {
        await widget.viewModel.update(user, username: _username.text.trim(), email: _email.text.trim(), nombreCompleto: _name.text.trim(), dni: _dni.text.trim(), telefono: _phone.text.trim(), roleId: _roleId!, activo: _active);
      }
      if (!mounted) return;
      Navigator.of(context).pop();
    } catch (ex) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(ex.toString())));
      setState(() => _saving = false);
    }
  }
}

class _Field extends StatelessWidget {
  const _Field({required this.controller, required this.label, required this.icon, this.obscure = false});
  final TextEditingController controller;
  final String label;
  final IconData icon;
  final bool obscure;
  @override
  Widget build(BuildContext context) => Padding(padding: const EdgeInsets.only(bottom: 10), child: TextFormField(controller: controller, obscureText: obscure, decoration: InputDecoration(prefixIcon: Icon(icon), labelText: label), validator: (value) => value == null || value.trim().isEmpty ? 'Requerido' : null));
}

class _HeroPill extends StatelessWidget {
  const _HeroPill(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)), child: Text(label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)));
}

class _LoadingCard extends StatelessWidget {
  const _LoadingCard();
  @override
  Widget build(BuildContext context) => const Card(child: Padding(padding: EdgeInsets.all(18), child: Row(children: [SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2)), SizedBox(width: 12), Text('Cargando usuarios...')])));
}

class _EmptyUsersCard extends StatelessWidget {
  const _EmptyUsersCard();
  @override
  Widget build(BuildContext context) => const Card(child: Padding(padding: EdgeInsets.all(18), child: Text('No hay usuarios registrados.')));
}
