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
    _viewModel = AuthViewModel(AuthService(ApiClient()))
      ..addListener(_onViewModelChanged);
    _healthViewModel = HealthViewModel(HealthService(ApiClient()))
      ..addListener(_onViewModelChanged);
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
                colors: [
                  Color(0xFFE0F7FF),
                  Color(0xFFF8FCFF),
                  Color(0xFFBAE6FD)
                ],
              ),
            ),
            child: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: EdgeInsets.symmetric(
                      horizontal: wide ? 28 : 22, vertical: 22),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(maxWidth: wide ? 980 : 430),
                    child: wide
                        ? _WideLogin(form: _buildForm(context), status: _status)
                        : _MobileLogin(
                            form: _buildForm(context), status: _status),
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
    if (_healthViewModel.loading && _healthViewModel.status == null) {
      return const _LoginStatus(
          'Revisando', Icons.sync_rounded, Color(0xFF58716B));
    }
    if (_healthViewModel.status?.isHealthy == true) {
      return const _LoginStatus(
          'Disponible', Icons.verified_rounded, Color(0xFF0A8A78));
    }
    if (_healthViewModel.failed || _healthViewModel.status != null) {
      return const _LoginStatus(
          'Sin conexion', Icons.warning_amber_rounded, Color(0xFFE67700));
    }
    return const _LoginStatus(
        'Listo', Icons.lock_open_rounded, Color(0xFF0A8A78));
  }

  Widget _buildForm(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Bienvenido de nuevo',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.w900, color: const Color(0xFF0F2533))),
          const SizedBox(height: 5),
          Text('Ingrese sus credenciales para continuar.',
              textAlign: TextAlign.center,
              style: Theme.of(context)
                  .textTheme
                  .bodyMedium
                  ?.copyWith(color: Colors.black54)),
          const SizedBox(height: 22),
          TextFormField(
            controller: _usernameController,
            decoration: const InputDecoration(
                prefixIcon: Icon(Icons.person_outline_rounded),
                labelText: 'Usuario'),
            validator: (value) => value == null || value.trim().isEmpty
                ? 'Ingrese usuario'
                : null,
          ),
          const SizedBox(height: 14),
          TextFormField(
            controller: _passwordController,
            obscureText: true,
            decoration: const InputDecoration(
                prefixIcon: Icon(Icons.lock_outline_rounded),
                labelText: 'Clave'),
            validator: (value) =>
                value == null || value.isEmpty ? 'Ingrese clave' : null,
          ),
          const SizedBox(height: 8),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(
                onPressed: () {}, child: const Text('Recordar clave')),
          ),
          if (_viewModel.error != null) ...[
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                  color: colorScheme.error.withValues(alpha: 0.08),
                  borderRadius: BorderRadius.circular(14)),
              child: Text(_viewModel.error!,
                  style: TextStyle(
                      color: colorScheme.error, fontWeight: FontWeight.w700)),
            ),
            const SizedBox(height: 12),
          ],
          FilledButton.icon(
            onPressed: _viewModel.loading ? null : _login,
            icon: _viewModel.loading
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2))
                : const Icon(Icons.arrow_forward_rounded),
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
    return SizedBox(
      height: 730,
      child: Stack(
        children: [
          _LoginWaveHeader(status: status),
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              padding: const EdgeInsets.fromLTRB(22, 30, 22, 18),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius:
                    const BorderRadius.vertical(top: Radius.circular(34)),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF0369A1).withValues(alpha: 0.12),
                    blurRadius: 28,
                    offset: const Offset(0, -8),
                  )
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  form,
                  const SizedBox(height: 18),
                  const _SocialDivider(),
                  const SizedBox(height: 14),
                  const _SocialLoginRow(),
                  const SizedBox(height: 14),
                  TextButton(
                    onPressed: () {},
                    child: const Text('No tienes cuenta?  Crear cuenta'),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _LoginWaveHeader extends StatelessWidget {
  const _LoginWaveHeader({required this.status});

  final _LoginStatus status;

  @override
  Widget build(BuildContext context) => SizedBox(
        height: 335,
        child: Stack(children: [
          ClipPath(
            clipper: _WaveClipper(),
            child: Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [Color(0xFF17AFC5), Color(0xFF066B80)],
                ),
              ),
            ),
          ),
          Positioned(
            top: 28,
            left: 0,
            right: 0,
            child: Column(children: [
              const _BrandMark(size: 66),
              const SizedBox(height: 12),
              Text('EconoSalud',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                      color: Colors.white,
                      fontWeight: FontWeight.w900,
                      letterSpacing: 0.2)),
              const SizedBox(height: 8),
              _PulseBadge(status: status),
            ]),
          ),
        ]),
      );
}

class _WaveClipper extends CustomClipper<Path> {
  @override
  Path getClip(Size size) {
    return Path()
      ..lineTo(0, size.height * 0.58)
      ..cubicTo(size.width * 0.28, size.height * 0.44, size.width * 0.56,
          size.height * 0.76, size.width, size.height * 0.58)
      ..lineTo(size.width, 0)
      ..close();
  }

  @override
  bool shouldReclip(covariant CustomClipper<Path> oldClipper) => false;
}

class _SocialDivider extends StatelessWidget {
  const _SocialDivider();

  @override
  Widget build(BuildContext context) => Row(children: [
        Expanded(child: Divider(color: Colors.black.withValues(alpha: 0.10))),
        const Padding(
          padding: EdgeInsets.symmetric(horizontal: 14),
          child: Text('O',
              style: TextStyle(
                  color: Color(0xFF607D8B),
                  fontSize: 12,
                  fontWeight: FontWeight.w800)),
        ),
        Expanded(child: Divider(color: Colors.black.withValues(alpha: 0.10))),
      ]);
}

class _SocialLoginRow extends StatelessWidget {
  const _SocialLoginRow();

  @override
  Widget build(BuildContext context) => const Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _SocialCircle(label: 'G', color: Color(0xFFEA4335)),
          SizedBox(width: 28),
          _SocialCircle(label: 'f', color: Color(0xFF1877F2)),
          SizedBox(width: 28),
          _SocialCircle(label: '', color: Colors.black),
        ],
      );
}

class _SocialCircle extends StatelessWidget {
  const _SocialCircle({required this.label, required this.color});

  final String label;
  final Color color;

  @override
  Widget build(BuildContext context) => Container(
        width: 44,
        height: 44,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: Colors.white,
          shape: BoxShape.circle,
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.07),
              blurRadius: 14,
              offset: const Offset(0, 6),
            )
          ],
        ),
        child: Text(label,
            style: TextStyle(
                color: color, fontSize: 22, fontWeight: FontWeight.w900)),
      );
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
              boxShadow: [
                BoxShadow(
                    color: Colors.black.withValues(alpha: 0.05),
                    blurRadius: 28,
                    offset: const Offset(0, 14))
              ],
            ),
            child: _BrandPanel(compact: false, status: status),
          ),
        ),
        const SizedBox(width: 28),
        Expanded(
          child: Container(
            padding: const EdgeInsets.all(34),
            decoration: BoxDecoration(
                color: Colors.white, borderRadius: BorderRadius.circular(34)),
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
      padding: EdgeInsets.all(compact ? 22 : 34),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(compact ? 28 : 32),
        gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF38BDF8), Color(0xFF0369A1)]),
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(compact ? 28 : 32),
        child: Stack(
          children: [
            Positioned(
                right: -34,
                top: -30,
                child: _Blob(
                    size: compact ? 126 : 180,
                    color: Colors.white.withValues(alpha: 0.22))),
            Positioned(
                left: -42,
                bottom: -44,
                child: _Blob(
                    size: compact ? 128 : 190,
                    color: Colors.white.withValues(alpha: 0.08))),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _BrandMark(size: compact ? 56 : 76),
                    const Spacer(),
                    Flexible(child: _PulseBadge(status: status)),
                  ],
                ),
                const Spacer(),
                Text('EconoSalud',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        color: Colors.white, fontWeight: FontWeight.w900)),
                const SizedBox(height: 4),
                Text('Botica movil',
                    maxLines: 1,
                    overflow: TextOverflow.visible,
                    style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.w900,
                        height: 1.0,
                        fontSize: compact ? 30 : null)),
                if (!compact) ...[
                  const SizedBox(height: 12),
                  const Text(
                      'Gestion rapida para ventas, inventario y reportes.',
                      style: TextStyle(color: Colors.white70, fontSize: 16)),
                ],
              ],
            ),
          ],
        ),
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
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 7),
      decoration: BoxDecoration(
          color: status.color.withValues(alpha: 0.20),
          borderRadius: BorderRadius.circular(999),
          border: Border.all(color: Colors.white.withValues(alpha: 0.18))),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(status.icon, color: Colors.white, size: 16),
          const SizedBox(width: 6),
          Flexible(
              child: Text(status.label,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w800,
                      fontSize: 11))),
        ],
      ),
    );
  }
}

class _BrandMark extends StatelessWidget {
  const _BrandMark({required this.size});

  final double size;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
          color: Colors.white, borderRadius: BorderRadius.circular(20)),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(14),
        child: Image.asset('assets/images/econo_salud_symbol.jpg',
            fit: BoxFit.cover),
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
    return Container(
        width: size,
        height: size,
        decoration: BoxDecoration(color: color, shape: BoxShape.circle));
  }
}

class _LoginStatus {
  const _LoginStatus(this.label, this.icon, this.color);
  final String label;
  final IconData icon;
  final Color color;
}
