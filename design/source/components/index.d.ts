import type * as React from 'react';

export type IconName = 'home' | 'search' | 'bell' | 'user' | 'settings' | 'plus' | 'close' | 'check' | 'chevron-left' | 'chevron-right' | 'chevron-up' | 'chevron-down' | 'arrow-left' | 'arrow-right' | 'menu' | 'more' | 'grid' | 'heart' | 'star' | 'edit' | 'trash' | 'info' | 'alert' | 'check-circle' | 'x-circle' | 'eye' | 'calendar' | 'mail' | 'lock' | 'logout' | 'filter' | 'upload' | 'download';
export type Tone = 'info' | 'success' | 'warning' | 'danger';

export interface IconProps { name: IconName; size?: number; strokeWidth?: number; label?: string; className?: string }
export declare function Icon(props: IconProps): React.ReactElement;

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'link';
  size?: 'sm' | 'md' | 'lg';
  iconLeft?: IconName | React.ReactNode; iconRight?: IconName | React.ReactNode;
  loading?: boolean; block?: boolean; as?: 'a' | 'button'; href?: string;
}
export declare function Button(props: ButtonProps): React.ReactElement;

export interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  icon: IconName | React.ReactNode; label: string;
  variant?: 'ghost' | 'secondary' | 'primary'; size?: 'sm' | 'md'; round?: boolean;
}
export declare function IconButton(props: IconButtonProps): React.ReactElement;

export interface TextFieldProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string; hint?: string; error?: string;
  leadingIcon?: IconName | React.ReactNode; trailing?: React.ReactNode; multiline?: boolean;
}
export declare function TextField(props: TextFieldProps): React.ReactElement;

export interface CheckboxProps extends React.InputHTMLAttributes<HTMLInputElement> { label?: React.ReactNode }
export declare function Checkbox(props: CheckboxProps): React.ReactElement;

export interface SwitchProps extends React.InputHTMLAttributes<HTMLInputElement> { label?: React.ReactNode }
export declare function Switch(props: SwitchProps): React.ReactElement;

export interface TabsProps {
  items: { id: string; label: string }[];
  value?: string; defaultValue?: string; onChange?: (id: string) => void;
  variant?: 'underline' | 'segmented'; block?: boolean; label?: string; className?: string;
}
export declare function Tabs(props: TabsProps): React.ReactElement;

export interface BadgeProps { tone?: 'neutral' | 'accent' | Tone; dot?: boolean; icon?: IconName | React.ReactNode; children?: React.ReactNode; className?: string }
export declare function Badge(props: BadgeProps): React.ReactElement;

export interface AvatarProps { name: string; src?: string; size?: 'xs' | 'sm' | 'md' | 'lg'; className?: string }
export declare function Avatar(props: AvatarProps): React.ReactElement;

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  title?: React.ReactNode; subtitle?: React.ReactNode; actions?: React.ReactNode; footer?: React.ReactNode;
  raised?: boolean; interactive?: boolean;
}
export declare function Card(props: CardProps): React.ReactElement;

export interface ListItemProps {
  title: React.ReactNode; subtitle?: React.ReactNode;
  leadingIcon?: IconName; leading?: React.ReactNode; trailing?: React.ReactNode; chevron?: boolean;
  onClick?: React.MouseEventHandler; href?: string; className?: string;
}
export declare function ListItem(props: ListItemProps): React.ReactElement;

export interface AlertProps { tone?: Tone; title?: React.ReactNode; children?: React.ReactNode; icon?: IconName; action?: React.ReactNode; className?: string }
export declare function Alert(props: AlertProps): React.ReactElement;

export interface ToastProps { children: React.ReactNode; tone?: Tone; actionLabel?: string; onAction?: () => void; className?: string }
export declare function Toast(props: ToastProps): React.ReactElement;

export interface DialogProps {
  title: React.ReactNode; children?: React.ReactNode; actions?: React.ReactNode;
  open?: boolean; onClose?: () => void; variant?: 'center' | 'sheet'; contained?: boolean; className?: string;
}
export declare function Dialog(props: DialogProps): React.ReactElement | null;

export interface BottomNavProps {
  items: { id: string; label: string; icon: IconName; badge?: number | string }[];
  value?: string; defaultValue?: string; onChange?: (id: string) => void; label?: string; className?: string;
}
export declare function BottomNav(props: BottomNavProps): React.ReactElement;

declare global {
  interface Window {
    TS: {
      Button: typeof Button; IconButton: typeof IconButton; TextField: typeof TextField; Checkbox: typeof Checkbox;
      Switch: typeof Switch; Tabs: typeof Tabs; Badge: typeof Badge; Avatar: typeof Avatar; Card: typeof Card;
      ListItem: typeof ListItem; Alert: typeof Alert; Toast: typeof Toast; Dialog: typeof Dialog;
      BottomNav: typeof BottomNav; Icon: typeof Icon & { names: IconName[] };
    };
  }
}
