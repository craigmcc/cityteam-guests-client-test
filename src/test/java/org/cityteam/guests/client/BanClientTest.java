/*
 * Copyright 2020 CityTeam, craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cityteam.guests.client;

import org.cityteam.guests.model.Ban;
import org.craigmcc.library.shared.exception.NotFound;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

public class BanClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final BanClient banClient = new BanClient();

    // Lifecycle Methods -----------------------------------------------------

    @Before
    public void before() {
        if ((depopulateEnabled == null) || (TRUE == depopulateEnabled)) {
            depopulate();
        }
        if ((populateEnabled == null) || (TRUE == populateEnabled)) {
            populate();
        }
    }

    // Test Methods ----------------------------------------------------------

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Ban> bans = banClient.findAll();
        assertThat(bans.size(), is(greaterThan(0)));

        for (Ban ban : bans) {

            // Delete and verify we can no longer retrieve it
            banClient.delete(ban.getId());
            assertThrows(NotFound.class,
                    () -> banClient.find(ban.getId()));
        }

        assertThat(banClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> banClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Ban> bans = banClient.findAll();
        assertThat(bans.size(), is(greaterThan(0)));

        for (Ban ban : bans) {
            Ban found = banClient.find(ban.getId());
            assertThat(found.equals(ban), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> banClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Ban> bans = banClient.findAll();
        assertThat(bans.size(), is(greaterThan(0)));

        String previousKey = null;
        for (Ban ban : bans) {
            String thisKey = "" + ban.getGuestId() + "|" + ban.getBanFrom();
            if (previousKey != null) {
                assertThat(thisKey, is(greaterThan(previousKey)));
            }
            previousKey = thisKey;
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Ban> bans = banClient.findAll();
        assertThat(bans.size(), is(greaterThan(0)));
        Ban ban = newBan(bans.get(0).getGuestId());
        Ban inserted = banClient.insert(ban);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(inserted.getActive(), is(ban.getActive()));
        assertThat(inserted.getBanFrom(), is(ban.getBanFrom()));
        assertThat(inserted.getBanTo(), is(ban.getBanTo()));
        assertThat(inserted.getGuestId(), is(ban.getGuestId()));
        assertThat(inserted.getStaff(), is(ban.getStaff()));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Ban> bans = banClient.findAll();
        Ban ban = newBan(bans.get(0).getGuestId());
        Ban inserted = banClient.insert(ban);

        inserted.setActive(!inserted.getActive());
        banClient.update(inserted.getId(), inserted);

    }

    // Support Methods -------------------------------------------------------

    private Ban newBan(Long guestId) {
        return new Ban(
                true,
                LocalDate.parse("2021-10-04"),
                LocalDate.parse("2021-10-31"),
                "New Ban Comments",
                guestId,
                "New Ban Staff"
        );
    }

}
