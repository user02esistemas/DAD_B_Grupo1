import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../dashboard/view/home_page.dart';
import '../../health/service/health_service.dart';
import '../../health/viewmodel/health_viewmodel.dart';
import '../model/user.dart';
import '../service/auth_service.dart';
import '../viewmodel/auth_viewmodel.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _usernameController = TextEditingController(text: 'admin');
  final _passwordController = TextEditingController(text: 'admin');
  late final AuthViewModel _viewModel;
  late final HealthViewModel _healthViewModel;

  @override
  void initState() {
    super.initState();
    _viewModel = AuthViewModel(AuthService(ApiClient()))..addListener(_onViewModelChanged);
    _healthViewModel = HealthViewModel(HealthService(ApiClient()))..addListener(_onViewModelChanged);
    _healthViewModel.check();
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    _healthViewModel.removeListener(_onViewModelChanged);
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) setState(() {});
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) return;

    final user = await _viewModel.login(
      _usernameController.text.trim(),
      _passwordController.text,
    );

    if (!mounted || user == null) return;
    _openHome(user);
  }

  void _openHome(User user) {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => HomePage(user: user)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: LayoutBuilder(
        builder: (context, constraints) {
          final wide = constraints.maxWidth >= 760;
          return Container(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [Color(0xFFCDEFE7), Color(0xFFF7FCFA), Color(0xFFDDF5EF)],
              ),
            ),
            child: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: EdgeInsets.symmetric(horizontal: wide ? 28 : 22, vertical: 22),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(maxWidth: wide ? 980 : 430),
                    child: wide ? _WideLogin(form: _buildForm(context), status: _status) : _MobileLogin(form: _buildForm(context), status: _status),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  _LoginStatus get _status {
    if (_healthViewModel.loading && _healthViewModel.status == null) return const _LoginStatus('Verificando servicio', Icons.sync_rounded, Color(0xFF58716B));
    if (_healthViewModel.status?.isHealthy == true) return const _LoginStatus('Sistema disponible', Icons.verified_rounded, Color(0xFF0A8A78));
    if (_healthViewModel.failed || _healthViewModel.status != null) return const _LoginStatus('Servicio no disponible', Icons.warning_amber_rounded, Color(0xFFE67700));
    return const _LoginStatus('Listo para ingresar', Icons.lock_open_rounded, Color(0xFF0A8A78));
  }

  Widget _buildForm(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Iniciar sesion', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900, color: const Color(0xFF087B68))),
          const SizedBox(height: 5),
          Text('Ingrese sus credenciales para continuar.', style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.black54)),
          const SizedBox(height: 22),
          TextFormField(
            controller: _usernameController,
            decoration: const InputDecoration(prefixIcon: Icon(Icons.person_outline_rounded), labelText: 'Usuario'),
            validator: (value) => value == null || value.trim().isEmpty ? 'Ingrese usuario' : null,
          ),
          const SizedBox(height: 14),
          TextFormField(
            controller: _passwordController,
            obscureText: true,
            decoration: const InputDecoration(prefixIcon: Icon(Icons.lock_outline_rounded), labelText: 'Clave'),
            validator: (value) => value == null || value.isEmpty ? 'Ingrese clave' : null,
          ),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(onPressed: () {}, child: const Text('Recordar clave')),
          ),
          if (_viewModel.error != null) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: colorScheme.error.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(14)),
              child: Text(_viewModel.error!, style: TextStyle(color: colorScheme.error, fontWeight: FontWeight.w700)),
            ),
            const SizedBox(height: 12),
          ],
          FilledButton.icon(
            onPressed: _viewModel.loading ? null : _login,
            icon: _viewModel.loading ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2)) : const Icon(Icons.arrow_forward_rounded),
            label: const Text('Ingresar'),
          ),
        ],
      ),
    );
  }
}

class _MobileLogin extends StatelessWidget {
  const _MobileLogin({required this.form, required this.status});

  final Widget form;
  final _LoginStatus status;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.94),
        borderRadius: BorderRadius.circular(30),
        boxShadow: [BoxShadow(color: const Color(0xFF087B68).withValues(alpha: 0.10), blurRadius: 32, offset: const Offset(0, 18))],
      ),
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            SizedBox(height: 250, child: _BrandPanel(compact: true, status: status)),
            const SizedBox(height: 22),
            form,
          ],
        ),
      ),
    );
  }
}

class _WideLogin extends StatelessWidget {
  const _WideLogin({required this.form, required this.status});

  final Widget form;
  final _LoginStatus status;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Container(
            height: 620,
            padding: const EdgeInsets.all(34),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.42),
              borderRadius: BorderRadius.circular(34),
              boxShadow: [BoxShadow(color: Colors.black.withValues(alpha: 0.05), blurRadius: 28, offset: const Offset(0, 14))],
            ),
            child: _BrandPanel(compact: false, status: status),
          ),
        ),
        const SizedBox(width: 28),
        Expanded(
          child: Container(
            padding: const EdgeInsets.all(34),
            decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(34)),
            child: form,
          ),
        ),
      ],
    );
  }
}

class _BrandPanel extends StatelessWidget {
  const _BrandPanel({required this.compact, required this.status});

  final bool compact;
  final _LoginStatus status;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(compact ? 24 : 34),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(compact ? 28 : 32),
        gradient: const LinearGradient(begin: Alignment.topLeft, end: Alignment.bottomRight, colors: [Color(0xFF087B68), Color(0xFF18BFA3)]),
      ),
      child: Stack(
        children: [
          Positioned(right: -26, top: -24, child: _Blob(size: compact ? 124 : 180, color: Colors.white.withValues(alpha: 0.22))),
          Positioned(left: -38, bottom: -34, child: _Blob(size: compact ? 128 : 190, color: Colors.white.withValues(alpha: 0.08))),
          Positioned(right: compact ? 12 : 20, bottom: compact ? 8 : 18, child: _PulseBadge(status: status)),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: compact ? 58 : 72,
                height: compact ? 58 : 72,
                decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(20)),
                child: Icon(Icons.local_pharmacy_rounded, color: Colors.white, size: compact ? 32 : 40),
              ),
              const Spacer(),
              Text('EconoSalud', style: Theme.of(context).textTheme.headlineSmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
              const SizedBox(height: 8),
              Text('Botica movil', style: Theme.of(context).textTheme.displaySmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900, height: 0.98)),
              if (!compact) ...[
                const SizedBox(height: 12),
                const Text('Gestion rapida para ventas, inventario y reportes.', style: TextStyle(color: Colors.white70, fontSize: 16)),
              ],
            ],
          ),
        ],
      ),
    );
  }
}

class _PulseBadge extends StatelessWidget {
  const _PulseBadge({required this.status});

  final _LoginStatus status;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(999)),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(status.icon, color: Colors.white, size: 16),
          const SizedBox(width: 6),
          Text(status.label, style: const TextStyle(color: Colors.white, fontWeight: FontWeight.w800, fontSize: 12)),
        ],
      ),
    );
  }
}

class _Blob extends StatelessWidget {
  const _Blob({required this.size, required this.color});

  final double size;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(width: size, height: size, decoration: BoxDecoration(color: color, shape: BoxShape.circle));
  }
}

class _LoginStatus {
  const _LoginStatus(this.label, this.icon, this.color);
  final String label;
  final IconData icon;
  final Color color;
}
