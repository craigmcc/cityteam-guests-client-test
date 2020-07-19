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

import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class GuestClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final FacilityClient facilityClient = new FacilityClient();
    private final GuestClient guestClient = new GuestClient();

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

        List<Guest> guests = guestClient.findAll();
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {

            // Delete and verify we can no longer retrieve it
            guestClient.delete(guest.getId());
            assertThrows(NotFound.class,
                    () -> guestClient.find(guest.getId()));
        }

        assertThat(guestClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> guestClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Guest> guests = guestClient.findAll();
        assertThat(guests.size(), is(greaterThan(0)));

        for (Guest guest : guests) {
            Guest found = guestClient.find(guest.getId());
            assertThat(found.equals(guest), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> guestClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Guest> guests = guestClient.findAll();
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            String thisName = guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");
        Guest guest = newGuest(facility.getId());
        Guest inserted = guestClient.insert(guest);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(inserted.getFirstName(), is(guest.getFirstName()));
        assertThat(inserted.getLastName(), is(guest.getLastName()));

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Portland");

        // Completely empty instance
        final Guest guest0 = new Guest();
        assertThrows(BadRequest.class,
                () -> guestClient.insert(guest0));

        // Missing firstName field
        final Guest guest1 = newGuest(facility.getId());
        guest1.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> guestClient.insert(guest1));

        // Missing lastName field
        final Guest guest2 = newGuest(facility.getId());
        guest1.setLastName(null);
        assertThrows(BadRequest.class,
                () -> guestClient.insert(guest2));

    }

    @Test
    public void insertNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");
        Guest guest = newGuest(facility.getId());
        Guest inserted = guestClient.insert(guest);

        assertThrows(NotUnique.class,
                () -> guestClient.insert(guest));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Francisco");

        // Change something but keep the name
        Guest guest1 = facilityClient.findGuestsByNameExact
                (facility.getId(), "Fred", "Flintstone");
        guest1.setComments(guest1.getComments() + " Updated");
        guestClient.update(guest1.getId(), guest1);

        // Change name to something unique
        Guest guest2 = facilityClient.findGuestsByNameExact
                (facility.getId(), "Barney", "Rubble");
        guest2.setFirstName("Unique First Name");
        guest2.setLastName("Unique Last Name");
        guestClient.update(guest2.getId(), guest2);

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Jose");

        // Required firstName
        Guest guest1 = facilityClient.findGuestsByNameExact
                (facility.getId(), "Bam Bam", "Rubble");
        guest1.setFirstName(null);
        assertThrows(BadRequest.class,
                () -> guestClient.update(guest1.getId(), guest1));

        // Required lastName
        Guest guest2 = facilityClient.findGuestsByNameExact
                (facility.getId(), "Bam Bam", "Rubble");
        guest1.setLastName(null);
        assertThrows(BadRequest.class,
                () -> guestClient.update(guest2.getId(), guest2));

    }

    @Test
    public void updateNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility1 = facilityClient.findByNameExact("Chester");
        Facility facility2 = facilityClient.findByNameExact("Portland");

        // Violate name uniqueness within facility
        Guest guest1 = facilityClient.findGuestsByNameExact
                (facility1.getId(), "Bam Bam", "Rubble");
        guest1.setFirstName("Barney");
        assertThrows(NotUnique.class,
                () -> guestClient.update(guest1.getId(), guest1));

        // Violate name uniqueness across facilities
        Guest guest2 = facilityClient.findGuestsByNameExact
                (facility1.getId(), "Bam Bam", "Rubble");
        guest1.setFacilityId(facility2.getId());
        assertThrows(NotUnique.class,
                () -> guestClient.update(guest2.getId(), guest2));

    }

    // Support Methods -------------------------------------------------------

    private Guest newGuest(Long facilityId) {
        return new Guest(
                "George Comment",
                facilityId,
                "George",
                "Jetson"
        );
    }

}
