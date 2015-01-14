/*
 * Copyright (C) 2014 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.affirmations;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.pgp.affirmation.LinkedIdentity;
import org.sufficientlysecure.keychain.pgp.affirmation.resources.DnsResource;

public class AffirmationCreateDnsStep1Fragment extends Fragment {

    AffirmationWizard mAffirmationWizard;

    EditText mEditDns;

    /**
     * Creates new instance of this fragment
     */
    public static AffirmationCreateDnsStep1Fragment newInstance() {
        AffirmationCreateDnsStep1Fragment frag = new AffirmationCreateDnsStep1Fragment();

        Bundle args = new Bundle();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAffirmationWizard = (AffirmationWizard) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.affirmation_create_dns_fragment_step1, container, false);

        view.findViewById(R.id.next_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String uri = mEditDns.getText().toString();

                if (!checkUri(uri)) {
                    mEditDns.setError("Please enter a valid domain name!");
                    return;
                }

                String proofNonce = LinkedIdentity.generateNonce();
                String proofText = DnsResource.generateText(getActivity(),
                        mAffirmationWizard.mFingerprint, proofNonce);

                AffirmationCreateDnsStep2Fragment frag =
                        AffirmationCreateDnsStep2Fragment.newInstance(uri, proofNonce, proofText);

                mAffirmationWizard.loadFragment(null, frag, AffirmationWizard.FRAG_ACTION_TO_RIGHT);

            }
        });

        view.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAffirmationWizard.loadFragment(null, null, AffirmationWizard.FRAG_ACTION_TO_LEFT);
            }
        });

        mEditDns = (EditText) view.findViewById(R.id.affirmation_create_dns_domain);

        mEditDns.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String uri = editable.toString();
                if (uri.length() > 0) {
                    if (checkUri(uri)) {
                        mEditDns.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.uid_mail_ok, 0);
                    } else {
                        mEditDns.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                                R.drawable.uid_mail_bad, 0);
                    }
                } else {
                    // remove drawable if email is empty
                    mEditDns.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }
        });

        mEditDns.setText("mugenguild.com");

        return view;
    }

    private static boolean checkUri(String uri) {
        return Patterns.DOMAIN_NAME.matcher(uri).matches();
    }

}
