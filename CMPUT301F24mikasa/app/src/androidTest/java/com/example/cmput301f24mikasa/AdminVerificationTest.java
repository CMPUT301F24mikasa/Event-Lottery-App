package com.example.cmput301f24mikasa;

import android.content.Context;
import android.provider.Settings;
import android.widget.ImageButton;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class AdminVerificationTest {

    private Context context;
    private FirebaseFirestore mockFirestore;
    private ImageButton mockAdminButton;



    @Before
    public void setUp() {
        // Initialize context and mocks
        context = ApplicationProvider.getApplicationContext();
        mockFirestore = mock(FirebaseFirestore.class); // Mock Firestore
        mockAdminButton = mock(ImageButton.class);     // Mock the ImageButton
    }

    @Test
    public void testAdminCheck_AdminExists() throws InterruptedException {
        String fakeDeviceId = "20b995b2c3ee4bb0";

        // Mock Firestore components
        CollectionReference mockCollectionReference = mock(CollectionReference.class);
        DocumentReference mockDocumentReference = mock(DocumentReference.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        when(mockFirestore.collection("admin")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(fakeDeviceId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.exists()).thenReturn(true);

        // Simulate task completion with CountDownLatch
        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            ((OnCompleteListener<DocumentSnapshot>) invocation.getArgument(0)).onComplete(mockTask);
            latch.countDown();
            return null;
        }).when(mockTask).addOnCompleteListener(any());

        // Call the updated method with mockFirestore
        AdminVerification.checkIfAdmin(context, mockAdminButton, fakeDeviceId, mockFirestore);

        // Wait for async operation to complete
        latch.await(2, TimeUnit.SECONDS);

        // Verify the ImageButton behavior
        verify(mockAdminButton).setVisibility(ImageButton.VISIBLE);
    }


    @Test
    public void testAdminCheck_AdminDoesNotExist() {
        // Simulate non-admin device ID
        String fakeDeviceId = "nonAdminDevice123";

        // Mock Firestore behavior for non-admin document check
        CollectionReference mockCollectionReference = mock(CollectionReference.class);
        DocumentReference mockDocumentReference = mock(DocumentReference.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);
        DocumentSnapshot mockDocumentSnapshot = mock(DocumentSnapshot.class);

        // Simulate Firestore behavior for non-admin document check
        when(mockFirestore.collection("admin")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(fakeDeviceId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);
        when(mockTask.getResult()).thenReturn(mockDocumentSnapshot);
        when(mockDocumentSnapshot.exists()).thenReturn(false);

        // Call the method to test
        AdminVerification.checkIfAdmin(context, mockAdminButton, fakeDeviceId, mockFirestore);

        // Verify that the ImageButton is hidden
        verify(mockAdminButton).setVisibility(8);  // 8 corresponds to GONE
    }

    @Test
    public void testAdminCheck_FirestoreError() {
        // Simulate device ID
        String fakeDeviceId = "errorDevice123";

        // Mock Firestore behavior for error case
        CollectionReference mockCollectionReference = mock(CollectionReference.class);
        DocumentReference mockDocumentReference = mock(DocumentReference.class);
        Task<DocumentSnapshot> mockTask = mock(Task.class);

        // Simulate Firestore failure
        when(mockFirestore.collection("admin")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(fakeDeviceId)).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(false); // Simulate Firestore failure

        // Call the method to test
        AdminVerification.checkIfAdmin(context, mockAdminButton, fakeDeviceId, mockFirestore);

        // Verify that the ImageButton remains hidden due to error
        verify(mockAdminButton).setVisibility(8);  // 8 corresponds to GONE
    }
}
