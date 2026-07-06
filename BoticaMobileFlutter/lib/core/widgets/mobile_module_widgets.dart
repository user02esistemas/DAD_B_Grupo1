import 'package:flutter/material.dart';

const brandRed = Color(0xFFE42313);
const brandDark = Color(0xFF2D1715);
const softBackground = Color(0xFFFFF7F5);

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
        color: Colors.white,
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
                style: Theme.of(context)
                    .textTheme
                    .titleLarge
                    ?.copyWith(fontWeight: FontWeight.w900, color: brandDark)),
            const SizedBox(height: 4),
            Text(subtitle,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(color: Colors.black54)),
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
          title:
              Text(title, style: const TextStyle(fontWeight: FontWeight.w900)),
          subtitle:
              Text(subtitle, maxLines: 2, overflow: TextOverflow.ellipsis),
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
    super.key,
  });

  final String title;
  final String subtitle;
  final String value;
  final String? footnote;
  final IconData icon;
  final Color color;
  final VoidCallback? onTap;

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
                          style: const TextStyle(fontWeight: FontWeight.w900)),
                      Text(subtitle,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(color: Colors.black54)),
                      if (footnote != null)
                        Text(footnote!,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                                color: Colors.black45, fontSize: 12)),
                    ]),
              ),
              const SizedBox(width: 8),
              Text(value,
                  style: TextStyle(color: color, fontWeight: FontWeight.w900)),
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
