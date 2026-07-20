import 'package:flutter/material.dart';

const brandRed = Color(0xFF38BDF8);
const brandSky = Color(0xFF38BDF8);
const brandSkyDark = Color(0xFF0369A1);
const brandDark = Color(0xFF0F2533);
const softBackground = Color(0xFFEFF8FF);
const darkBackground = Color(0xFF08212C);
const darkSurface = Color(0xFF123241);
const darkSurfaceSoft = Color(0xFF183D4E);

bool isDarkMode(BuildContext context) =>
    Theme.of(context).brightness == Brightness.dark;

Color moduleSurface(BuildContext context) =>
    isDarkMode(context) ? darkSurface : Colors.white;

Color moduleTextPrimary(BuildContext context) =>
    isDarkMode(context) ? const Color(0xFFEAF8FF) : brandDark;

Color moduleTextSecondary(BuildContext context) =>
    isDarkMode(context) ? const Color(0xFFB7CFDA) : Colors.black54;

Color moduleTextMuted(BuildContext context) =>
    isDarkMode(context) ? const Color(0xFF89A7B5) : Colors.black45;

class CompactModuleHeader extends StatelessWidget {
  const CompactModuleHeader({
    required this.title,
    required this.subtitle,
    required this.icon,
    this.trailing,
    this.color = brandRed,
    super.key,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final Widget? trailing;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: moduleSurface(context),
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: color.withValues(alpha: 0.08)),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.08),
            blurRadius: 24,
            offset: const Offset(0, 12),
          )
        ],
      ),
      child: Row(children: [
        Container(
          width: 52,
          height: 52,
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(18),
          ),
          child: Icon(icon, color: color, size: 28),
        ),
        const SizedBox(width: 14),
        Expanded(
          child:
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Text(title,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                    fontWeight: FontWeight.w900,
                    color: moduleTextPrimary(context))),
            const SizedBox(height: 4),
            Text(subtitle,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(color: moduleTextSecondary(context))),
          ]),
        ),
        if (trailing != null) ...[const SizedBox(width: 10), trailing!],
      ]),
    );
  }
}

class ActionTile extends StatelessWidget {
  const ActionTile({
    required this.title,
    required this.subtitle,
    required this.icon,
    required this.onTap,
    this.color = brandRed,
    super.key,
  });

  final String title;
  final String subtitle;
  final IconData icon;
  final VoidCallback? onTap;
  final Color color;

  @override
  Widget build(BuildContext context) => Card(
        child: ListTile(
          onTap: onTap,
          leading: CircleAvatar(
            backgroundColor: color.withValues(alpha: 0.12),
            child: Icon(icon, color: color),
          ),
          title: Text(title,
              style: TextStyle(
                  fontWeight: FontWeight.w900,
                  color: moduleTextPrimary(context))),
          subtitle: Text(subtitle,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(color: moduleTextSecondary(context))),
          trailing: const Icon(Icons.chevron_right_rounded),
        ),
      );
}

class CompactRecordCard extends StatelessWidget {
  const CompactRecordCard({
    required this.title,
    required this.subtitle,
    required this.value,
    this.footnote,
    this.icon = Icons.receipt_long_rounded,
    this.color = brandRed,
    this.onTap,
    this.trailing,
    super.key,
  });

  final String title;
  final String subtitle;
  final String value;
  final String? footnote;
  final IconData icon;
  final Color color;
  final VoidCallback? onTap;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) => Card(
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(24),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Row(children: [
              CircleAvatar(
                backgroundColor: color.withValues(alpha: 0.1),
                child: Icon(icon, color: color, size: 21),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                              fontWeight: FontWeight.w900,
                              color: moduleTextPrimary(context))),
                      Text(subtitle,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style:
                              TextStyle(color: moduleTextSecondary(context))),
                      if (footnote != null)
                        Text(footnote!,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                                color: moduleTextMuted(context), fontSize: 12)),
                    ]),
              ),
              const SizedBox(width: 8),
              Text(value,
                  style: TextStyle(color: color, fontWeight: FontWeight.w900)),
              if (trailing != null) ...[
                const SizedBox(width: 2),
                trailing!,
              ],
            ]),
          ),
        ),
      );
}

class PagerBar extends StatelessWidget {
  const PagerBar({
    required this.page,
    required this.totalPages,
    required this.onPrevious,
    required this.onNext,
    super.key,
  });

  final int page;
  final int totalPages;
  final VoidCallback? onPrevious;
  final VoidCallback? onNext;

  @override
  Widget build(BuildContext context) => Row(children: [
        IconButton.filledTonal(
            onPressed: onPrevious,
            icon: const Icon(Icons.chevron_left_rounded)),
        Expanded(
          child: Center(
            child: Text('Pagina $page de $totalPages',
                style: const TextStyle(fontWeight: FontWeight.w800)),
          ),
        ),
        IconButton.filledTonal(
            onPressed: onNext, icon: const Icon(Icons.chevron_right_rounded)),
      ]);
}

class ModuleAppTitle extends StatelessWidget {
  const ModuleAppTitle({required this.title, this.icon, super.key});

  final String title;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    final dark = isDarkMode(context);
    return Row(mainAxisSize: MainAxisSize.min, children: [
      Container(
        width: 8,
        height: 28,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(999),
          gradient: const LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [brandSky, brandSkyDark],
          ),
        ),
      ),
      const SizedBox(width: 9),
      if (icon != null) ...[
        Icon(icon, color: brandSkyDark, size: 22),
        const SizedBox(width: 7),
      ],
      Text(title,
          style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                color: dark ? const Color(0xFFEAF8FF) : brandDark,
                fontWeight: FontWeight.w900,
                letterSpacing: -0.6,
              )),
    ]);
  }
}

class AppQuickNavBar extends StatelessWidget {
  const AppQuickNavBar({
    required this.current,
    required this.onHome,
    required this.onAlerts,
    required this.onCaja,
    required this.onProfile,
    this.alerts = 0,
    super.key,
  });

  final String current;
  final VoidCallback onHome;
  final VoidCallback onAlerts;
  final VoidCallback onCaja;
  final VoidCallback onProfile;
  final int alerts;

  @override
  Widget build(BuildContext context) {
    final items = [
      _AppQuickNavItem('inicio', 'Inicio', Icons.home_rounded, onHome, 0),
      _AppQuickNavItem(
          'alertas', 'Alertas', Icons.notifications_rounded, onAlerts, alerts),
      _AppQuickNavItem(
          'caja', 'Caja', Icons.account_balance_wallet_rounded, onCaja, 0),
      _AppQuickNavItem('perfil', 'Perfil', Icons.person_rounded, onProfile, 0),
    ];

    return SafeArea(
      minimum: const EdgeInsets.fromLTRB(14, 0, 14, 10),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isDarkMode(context) ? darkSurfaceSoft : Colors.white,
          borderRadius: BorderRadius.circular(28),
          border: Border.all(
              color: brandSky.withValues(
                  alpha: isDarkMode(context) ? 0.18 : 0.10)),
          boxShadow: [
            BoxShadow(
              color: brandDark.withValues(
                  alpha: isDarkMode(context) ? 0.26 : 0.10),
              blurRadius: 24,
              offset: const Offset(0, 12),
            )
          ],
        ),
        child: Row(
          children: [
            for (final item in items)
              Expanded(
                child: _AppQuickNavButton(
                  item: item,
                  selected: item.keyName == current,
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _AppQuickNavItem {
  const _AppQuickNavItem(
      this.keyName, this.label, this.icon, this.onTap, this.badge);
  final String keyName;
  final String label;
  final IconData icon;
  final VoidCallback onTap;
  final int badge;
}

class _AppQuickNavButton extends StatelessWidget {
  const _AppQuickNavButton({required this.item, required this.selected});

  final _AppQuickNavItem item;
  final bool selected;

  @override
  Widget build(BuildContext context) {
    final active = isDarkMode(context) ? brandSky : brandSkyDark;
    final color = selected ? active : moduleTextSecondary(context);
    return InkWell(
      onTap: item.onTap,
      borderRadius: BorderRadius.circular(22),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          Stack(clipBehavior: Clip.none, children: [
            Container(
              width: 38,
              height: 30,
              decoration: BoxDecoration(
                color: selected
                    ? active.withValues(alpha: 0.14)
                    : Colors.transparent,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(item.icon, color: color, size: 22),
            ),
            if (item.badge > 0)
              Positioned(
                right: -5,
                top: -5,
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                  decoration: BoxDecoration(
                    color: const Color(0xFFFF8A00),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(item.badge > 9 ? '9+' : '${item.badge}',
                      style: const TextStyle(
                          color: Colors.white,
                          fontSize: 9,
                          fontWeight: FontWeight.w900)),
                ),
              ),
          ]),
          const SizedBox(height: 2),
          Text(item.label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                  color: color, fontSize: 11, fontWeight: FontWeight.w900)),
        ]),
      ),
    );
  }
}

void showQuickProfileSheet(
  BuildContext context, {
  required String name,
  required String username,
  required String role,
}) {
  showModalBottomSheet<void>(
    context: context,
    showDragHandle: true,
    builder: (_) => SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
        child: Row(children: [
          CircleAvatar(
            radius: 30,
            backgroundColor: brandSky.withValues(alpha: 0.16),
            child:
                const Icon(Icons.person_rounded, color: brandSkyDark, size: 32),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(name,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                        color: moduleTextPrimary(context),
                        fontWeight: FontWeight.w900,
                        fontSize: 18)),
                Text(username,
                    style: TextStyle(color: moduleTextSecondary(context))),
                const SizedBox(height: 6),
                Chip(
                  avatar: const Icon(Icons.verified_user_rounded,
                      size: 17, color: brandSkyDark),
                  label: Text(role),
                  backgroundColor: brandSky.withValues(alpha: 0.10),
                  side: BorderSide.none,
                ),
              ],
            ),
          ),
        ]),
      ),
    ),
  );
}
