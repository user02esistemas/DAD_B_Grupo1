import 'package:flutter/material.dart';

import '../../../core/network/api_client.dart';
import '../../dashboard/view/home_page.dart';
import '../../health/view/health_status_panel.dart';
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

  @override
  void initState() {
    super.initState();
    _viewModel = AuthViewModel(AuthService(ApiClient()))..addListener(_onViewModelChanged);
  }

  @override
  void dispose() {
    _viewModel.removeListener(_onViewModelChanged);
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  void _onViewModelChanged() {
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> _login() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    final user = await _viewModel.login(
      _usernameController.text.trim(),
      _passwordController.text,
    );

    if (!mounted || user == null) {
      return;
    }
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
                colors: [Color(0xFFB7E9DD), Color(0xFFEFF8F4), Color(0xFFD8F3EA)],
              ),
            ),
            child: SafeArea(
              child: Center(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(18),
                  child: ConstrainedBox(
                    constraints: BoxConstraints(maxWidth: wide ? 920 : 440),
                    child: wide ? _WideLogin(form: _buildForm(context)) : _MobileLogin(form: _buildForm(context)),
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  Widget _buildForm(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    return Form(
      key: _formKey,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Login', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w900, color: colorScheme.primary)),
          const SizedBox(height: 14),
          const HealthStatusPanel(),
          const SizedBox(height: 18),
          TextFormField(
            controller: _usernameController,
            decoration: const InputDecoration(prefixIcon: Icon(Icons.person_outline), labelText: 'Usuario'),
            validator: (value) => value == null || value.trim().isEmpty ? 'Ingrese usuario' : null,
          ),
          const SizedBox(height: 12),
          TextFormField(
            controller: _passwordController,
            obscureText: true,
            decoration: const InputDecoration(prefixIcon: Icon(Icons.lock_outline), labelText: 'Clave'),
            validator: (value) => value == null || value.isEmpty ? 'Ingrese clave' : null,
          ),
          Align(
            alignment: Alignment.centerRight,
            child: TextButton(onPressed: () {}, child: const Text('Recordar clave')),
          ),
          if (_viewModel.error != null) ...[
            Text(_viewModel.error!, style: TextStyle(color: colorScheme.error)),
            const SizedBox(height: 10),
          ],
          FilledButton.icon(
            onPressed: _viewModel.loading ? null : _login,
            icon: _viewModel.loading
                ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                : const Icon(Icons.arrow_forward_rounded),
            label: const Text('Ingresar'),
          ),
          const SizedBox(height: 18),
          Row(
            children: [
              const Expanded(child: Divider()),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 10),
                child: Text('API REST + RMI', style: Theme.of(context).textTheme.bodySmall?.copyWith(color: Colors.black45)),
              ),
              const Expanded(child: Divider()),
            ],
          ),
          const SizedBox(height: 14),
          const Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              _MiniBadge(icon: Icons.storage_rounded, label: 'MySQL'),
              SizedBox(width: 8),
              _MiniBadge(icon: Icons.api_rounded, label: 'API'),
              SizedBox(width: 8),
              _MiniBadge(icon: Icons.hub_rounded, label: 'RMI'),
            ],
          ),
        ],
      ),
    );
  }
}

class _MobileLogin extends StatelessWidget {
  const _MobileLogin({required this.form});

  final Widget form;

  @override
  Widget build(BuildContext context) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const SizedBox(height: 210, child: _OrganicHeader(compact: true)),
          Padding(padding: const EdgeInsets.fromLTRB(20, 18, 20, 22), child: form),
        ],
      ),
    );
  }
}

class _WideLogin extends StatelessWidget {
  const _WideLogin({required this.form});

  final Widget form;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: SizedBox(
        height: 620,
        child: Row(
          children: [
            const Expanded(child: _OrganicHeader(compact: false)),
            Expanded(
              child: Padding(padding: const EdgeInsets.all(34), child: form),
            ),
          ],
        ),
      ),
    );
  }
}

class _OrganicHeader extends StatelessWidget {
  const _OrganicHeader({required this.compact});

  final bool compact;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.all(compact ? 24 : 34),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(compact ? 34 : 28),
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF087B68), Color(0xFF16B89C)],
        ),
      ),
      child: Stack(
        children: [
          Positioned(right: -30, top: -20, child: _Blob(size: compact ? 120 : 170, color: Colors.white24)),
          Positioned(left: -34, bottom: -26, child: _Blob(size: compact ? 110 : 150, color: Colors.white12)),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 60,
                height: 60,
                decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.18), borderRadius: BorderRadius.circular(20)),
                child: const Icon(Icons.local_pharmacy_rounded, color: Colors.white, size: 34),
              ),
              const SizedBox(height: 24),
              Text('EconoSalud', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
              const SizedBox(height: 18),
              Text('Botica movil', style: Theme.of(context).textTheme.displaySmall?.copyWith(color: Colors.white, fontWeight: FontWeight.w900)),
              const SizedBox(height: 6),
              const Text('Bienvenido a EconoSalud', style: TextStyle(color: Colors.white70, fontSize: 16)),
              if (!compact) ...[
                const Spacer(),
                const _FeatureLine(icon: Icons.verified_rounded, text: 'Control de inventario y vencimientos'),
                const _FeatureLine(icon: Icons.point_of_sale_rounded, text: 'Ventas y compras distribuidas'),
                const _FeatureLine(icon: Icons.insights_rounded, text: 'Indicadores operativos en tiempo real'),
              ],
            ],
          ),
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

class _FeatureLine extends StatelessWidget {
  const _FeatureLine({required this.icon, required this.text});

  final IconData icon;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 12),
      child: Row(
        children: [
          Icon(icon, color: Colors.white, size: 20),
          const SizedBox(width: 10),
          Expanded(child: Text(text, style: const TextStyle(color: Colors.white))),
        ],
      ),
    );
  }
}

class _MiniBadge extends StatelessWidget {
  const _MiniBadge({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 8),
      decoration: BoxDecoration(color: const Color(0xFFE7F6EF), borderRadius: BorderRadius.circular(12)),
      child: Row(
        children: [
          Icon(icon, size: 16, color: Theme.of(context).colorScheme.primary),
          const SizedBox(width: 5),
          Text(label, style: TextStyle(color: Theme.of(context).colorScheme.primary, fontWeight: FontWeight.w800, fontSize: 12)),
        ],
      ),
    );
  }
}
