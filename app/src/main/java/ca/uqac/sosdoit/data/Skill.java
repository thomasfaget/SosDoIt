package ca.uqac.sosdoit.data;

import android.support.annotation.StringRes;

import ca.uqac.sosdoit.R;

/**
 * The list of all the skills possible for a User
 */
public enum Skill
{
    SKILL_TEST(R.string.app_name);

    @StringRes
    public int id;

    Skill(@StringRes int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }
}
