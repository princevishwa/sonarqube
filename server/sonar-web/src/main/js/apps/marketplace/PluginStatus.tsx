/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import * as React from 'react';
import { Plugin } from '../../api/plugins';
import PluginActions from './PluginActions';
import { translate } from '../../helpers/l10n';

interface Props {
  plugin: Plugin;
  refreshPending: () => void;
  status?: string;
}

export default function PluginStatus({ plugin, refreshPending, status }: Props) {
  return (
    <td className="text-top text-right width-20">
      {status === 'installing' && (
        <p className="text-success">{translate('marketplace.install_pending')}</p>
      )}

      {status === 'updating' && (
        <p className="text-success">{translate('marketplace.update_pending')}</p>
      )}

      {status === 'removing' && (
        <p className="text-danger">{translate('marketplace.uninstall_pending')}</p>
      )}

      {status == null && (
        <div>
          <i className="js-spinner spinner hidden" />
          <PluginActions plugin={plugin} refreshPending={refreshPending} />
        </div>
      )}
    </td>
  );
}