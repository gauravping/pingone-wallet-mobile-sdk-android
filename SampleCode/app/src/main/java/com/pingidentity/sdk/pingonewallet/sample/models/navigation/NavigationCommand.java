package com.pingidentity.sdk.pingonewallet.sample.models.navigation;

import androidx.navigation.NavDirections;

public abstract class NavigationCommand {

    public static class ToDirection extends NavigationCommand {

        public NavDirections directions;

        public ToDirection(NavDirections directions) {
            this.directions = directions;
        }

    }

    public static class Back extends NavigationCommand {

    }

}
