/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
package org.sonar.db.purge;

public final class PurgeSnapshotQuery {
  private String snapshotUuid;
  private String analysisUuid;
  private String rootComponentUuid;
  private String componentUuid;
  private String[] scopes;
  private String[] status;
  private Boolean islast;
  private Boolean notPurged;
  private Boolean withVersionEvent;

  private PurgeSnapshotQuery() {
  }

  public static PurgeSnapshotQuery create() {
    return new PurgeSnapshotQuery();
  }

  public String getSnapshotUuid() {
    return snapshotUuid;
  }

  public PurgeSnapshotQuery setSnapshotUuid(String snapshotUuid) {
    this.snapshotUuid = snapshotUuid;
    return this;
  }

  public String getAnalysisUuid() {
    return analysisUuid;
  }

  public PurgeSnapshotQuery setAnalysisUuid(String analysisUuid) {
    this.analysisUuid = analysisUuid;
    return this;
  }

  public String getRootComponentUuid() {
    return rootComponentUuid;
  }

  public PurgeSnapshotQuery setRootComponentUuid(String rootComponentUuid) {
    this.rootComponentUuid = rootComponentUuid;
    return this;
  }

  public String[] getScopes() {
    return scopes;// NOSONAR May expose internal representation by returning reference to mutable object
  }

  public PurgeSnapshotQuery setScopes(String[] scopes) {
    this.scopes = scopes; // NOSONAR May expose internal representation by incorporating reference to mutable object
    return this;
  }

  public String[] getStatus() {
    return status;// NOSONAR May expose internal representation by returning reference to mutable object
  }

  public PurgeSnapshotQuery setStatus(String[] status) {
    this.status = status; // NOSONAR org.sonar.db.purge.PurgeSnapshotQuery.setStatus(String[]) may expose internal representation
    return this;
  }

  public Boolean getIslast() {
    return islast;
  }

  public PurgeSnapshotQuery setIslast(Boolean islast) {
    this.islast = islast;
    return this;
  }

  public Boolean getNotPurged() {
    return notPurged;
  }

  public PurgeSnapshotQuery setNotPurged(Boolean notPurged) {
    this.notPurged = notPurged;
    return this;
  }

  public String getComponentUuid() {
    return componentUuid;
  }

  public PurgeSnapshotQuery setComponentUuid(String componentUuid) {
    this.componentUuid = componentUuid;
    return this;
  }

  public Boolean getWithVersionEvent() {
    return withVersionEvent;
  }

  public PurgeSnapshotQuery setWithVersionEvent(Boolean withVersionEvent) {
    this.withVersionEvent = withVersionEvent;
    return this;
  }
}
