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
    final colorScheme = Theme.of(context).colorScheme;

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFE7F6EF), Color(0xFFF8FBF8), Color(0xFFDDF0EA)],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(20),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 460),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(24),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          Container(
                            width: 76,
                            height: 76,
                            decoration: BoxDecoration(
                              color: colorScheme.primary,
                              borderRadius: BorderRadius.circular(24),
                              boxShadow: [
                                BoxShadow(
                                  color: colorScheme.primary.withValues(alpha: 0.25),
                                  blurRadius: 24,
                                  offset: const Offset(0, 12),
                                ),
                              ],
                            ),
                            child: const Icon(Icons.local_pharmacy_rounded, size: 42, color: Colors.white),
                          ),
                          const SizedBox(height: 22),
                          Text(
                            'EconoSalud',
                            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                  fontWeight: FontWeight.w800,
                                  color: const Color(0xFF12332D),
                                ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            'Gestion movil para botica conectada a la API REST.',
                            style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: Colors.black54),
                          ),
                          const SizedBox(height: 22),
                          const HealthStatusPanel(),
                          const SizedBox(height: 22),
                          TextFormField(
                            controller: _usernameController,
                            decoration: const InputDecoration(prefixIcon: Icon(Icons.person_outline), labelText: 'Usuario'),
                            validator: (value) => value == null || value.trim().isEmpty ? 'Ingrese usuario' : null,
                          ),
                          const SizedBox(height: 14),
                          TextFormField(
                            controller: _passwordController,
                            obscureText: true,
                            decoration: const InputDecoration(prefixIcon: Icon(Icons.lock_outline), labelText: 'Clave'),
                            validator: (value) => value == null || value.isEmpty ? 'Ingrese clave' : null,
                          ),
                          if (_viewModel.error != null) ...[
                            const SizedBox(height: 14),
                            Text(_viewModel.error!, style: TextStyle(color: colorScheme.error)),
                          ],
                          const SizedBox(height: 22),
                          FilledButton.icon(
                            onPressed: _viewModel.loading ? null : _login,
                            icon: _viewModel.loading
                                ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                                : const Icon(Icons.login_rounded),
                            label: const Text('Ingresar'),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
